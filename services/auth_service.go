package services

import (
	"context"
	"errors"
	"fmt"
	"kalasetu/models"
	"kalasetu/repos"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

var (
	ErrUserAlreadyExists = errors.New("user already exists")
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrInvalidToken      = errors.New("invalid token")
)

type AuthService interface {
	Register(ctx context.Context, input models.RegisterInput) (*models.AuthResponse, error)
	Login(ctx context.Context, input models.LoginInput) (*models.AuthResponse, error)
	RefreshToken(ctx context.Context, input models.RefreshInput) (*models.TokenResponse, error)
}

type authService struct {
	userRepo repos.UserRepository
}

func NewAuthService(userRepo repos.UserRepository) AuthService {
	return &authService{
		userRepo: userRepo,
	}
}

func (s *authService) Register(ctx context.Context, input models.RegisterInput) (*models.AuthResponse, error) {
	// Check if user already exists
	existing, err := s.userRepo.FindByEmail(ctx, input.Email)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return nil, ErrUserAlreadyExists
	}

	// Hash password
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(input.Password), bcrypt.DefaultCost)
	if err != nil {
		return nil, fmt.Errorf("failed to hash password: %w", err)
	}

	// Create user model
	user := &models.User{
		Email:    input.Email,
		Password: string(hashedPassword),
		Name:     input.Name,
	}

	savedUser, err := s.userRepo.Create(ctx, user)
	if err != nil {
		return nil, err
	}

	// Generate tokens
	accessToken, refreshToken, err := s.generateTokens(savedUser)
	if err != nil {
		return nil, err
	}

	return &models.AuthResponse{
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
		User:         *savedUser,
	}, nil
}

func (s *authService) Login(ctx context.Context, input models.LoginInput) (*models.AuthResponse, error) {
	user, err := s.userRepo.FindByEmail(ctx, input.Email)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, ErrInvalidCredentials
	}

	// Verify password
	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(input.Password))
	if err != nil {
		return nil, ErrInvalidCredentials
	}

	// Generate tokens
	accessToken, refreshToken, err := s.generateTokens(user)
	if err != nil {
		return nil, err
	}

	return &models.AuthResponse{
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
		User:         *user,
	}, nil
}

func (s *authService) RefreshToken(ctx context.Context, input models.RefreshInput) (*models.TokenResponse, error) {
	// Verify refresh token
	refreshSecret := os.Getenv("JWT_REFRESH_SECRET")
	if refreshSecret == "" {
		return nil, errors.New("JWT_REFRESH_SECRET env variable not set")
	}

	token, err := jwt.Parse(input.RefreshToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(refreshSecret), nil
	})

	if err != nil || !token.Valid {
		return nil, ErrInvalidToken
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		return nil, ErrInvalidToken
	}

	userIdFloat, ok := claims["user_id"].(float64)
	if !ok {
		return nil, ErrInvalidToken
	}
	userId := int(userIdFloat)

	// Verify user still exists
	user, err := s.userRepo.FindByID(ctx, userId)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, ErrInvalidToken
	}

	// Generate a new access token
	accessToken, err := s.generateAccessToken(user)
	if err != nil {
		return nil, err
	}

	return &models.TokenResponse{
		AccessToken: accessToken,
	}, nil
}

func (s *authService) generateTokens(user *models.User) (string, string, error) {
	accessToken, err := s.generateAccessToken(user)
	if err != nil {
		return "", "", err
	}

	refreshToken, err := s.generateRefreshToken(user)
	if err != nil {
		return "", "", err
	}

	return accessToken, refreshToken, nil
}

func (s *authService) generateAccessToken(user *models.User) (string, error) {
	accessSecret := os.Getenv("JWT_ACCESS_SECRET")
	if accessSecret == "" {
		return "", errors.New("JWT_ACCESS_SECRET env variable not set")
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"user_id": user.ID,
		"email":   user.Email,
		"exp":     time.Now().Add(time.Minute * 15).Unix(), // Access token expires in 15 minutes
	})

	return token.SignedString([]byte(accessSecret))
}

func (s *authService) generateRefreshToken(user *models.User) (string, error) {
	refreshSecret := os.Getenv("JWT_REFRESH_SECRET")
	if refreshSecret == "" {
		return "", errors.New("JWT_REFRESH_SECRET env variable not set")
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"user_id": user.ID,
		"exp":     time.Now().Add(time.Hour * 24 * 7).Unix(), // Refresh token expires in 7 days
	})

	return token.SignedString([]byte(refreshSecret))
}
