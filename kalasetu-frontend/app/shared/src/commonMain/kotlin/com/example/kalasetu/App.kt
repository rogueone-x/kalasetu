package com.example.kalasetu

import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.example.kalasetu.features.auth.*
import com.example.kalasetu.features.onboarding.*
import com.example.kalasetu.features.profile.*
import com.example.kalasetu.navigation.Screen
import com.example.kalasetu.theme.KalasetuTheme

@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.OnboardingWelcome) }
    var selectedRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf("") }
    var currentProfile by remember { mutableStateOf<Profile?>(null) }

    KalasetuTheme {
        when (val currentScreen = screen) {

            Screen.OnboardingWelcome -> OnboardingWelcomeScreen(
                onNext = { screen = Screen.AuthSignup }
            )

            Screen.AuthSignup -> AuthSignupScreen(
                onSignUp = { screen = Screen.AuthOtp },
                onLogin = { screen = Screen.AuthLogin },
                onBack = { screen = Screen.OnboardingWelcome }
            )

            Screen.AuthOtp -> AuthOtpScreen(
                onVerify = { screen = Screen.OnboardingBasicInfo },
                onLogin = { screen = Screen.AuthLogin },
                onBack = { screen = Screen.AuthSignup }
            )

            Screen.AuthLogin -> AuthLoginScreen(
                onLogin = { screen = Screen.OnboardingBasicInfo },
                onSignUp = { screen = Screen.AuthSignup },
                onBack = { screen = Screen.AuthSignup }
            )

            Screen.OnboardingBasicInfo -> OnboardingBasicInfoScreen(
                onNext = { name, role ->
                    userName = name
                    selectedRole = role
                    screen = Screen.OnboardingLocation
                },
            ) { screen = Screen.OnboardingWelcome }

            Screen.OnboardingLocation -> OnboardingLocationScreen(
                onNext = { location ->
                    userLocation = location
                    screen = when (selectedRole) {
                        "Artist"          -> Screen.ArtistExperience
                        "Event Organizer" -> Screen.OrganizerType
                        else              -> Screen.AudienceInterests
                    }
                },
            ) { screen = Screen.OnboardingBasicInfo }

            Screen.ArtistExperience -> ExperienceScreen(
                onNext = { screen = Screen.OnboardingDone },
            ) { screen = Screen.OnboardingLocation }

            Screen.OrganizerType -> OrganizerTypeScreen(
                onNext = { screen = Screen.OrganizerIntent },
            ) { screen = Screen.OnboardingLocation }

            Screen.OrganizerIntent -> OrganizerIntentScreen(
                onNext = { screen = Screen.OnboardingDone },
            ) { screen = Screen.OrganizerType }

            Screen.AudienceInterests -> InterestsScreen(
                onNext = { screen = Screen.OnboardingDone },
            ) { screen = Screen.OnboardingLocation }

            Screen.OnboardingDone -> OnboardingDoneScreen {
                // Temporary mock user ID for Profile UI development.
                // Replace with the authenticated user ID when registration/auth is integrated.
                screen = Screen.Profile(userId = "123")
            }

            is Screen.Profile -> {

                val presenter = remember(currentScreen.userId, currentProfile) {
                    ProfilePresenter(
                        repository = ProfileRepository(
                            initialProfile = currentProfile ?: Profile(
                                name = userName,
                                location = userLocation
                            )
                        )
                    )
                }
                ProfileScreen(
                    presenter = presenter,
                    userId = currentScreen.userId,
                    onEditProfile = { screen = Screen.EditProfile(currentScreen.userId) },
                    onShare = { /* Handle share */ },
                )
            }

            is Screen.EditProfile -> {
                val profileToEdit = currentProfile ?: Profile(
                    id       = currentScreen.userId,
                    name     = userName,
                    location = userLocation,
                    username = "",
                    email    = "",
                )
                EditProfileScreen(
                    profile = profileToEdit,
                    onBack  = { screen = Screen.Profile(userId = currentScreen.userId) },
                    onSave  = { updated ->
                        currentProfile = updated
                        userName       = updated.name
                        screen         = Screen.Profile(userId = currentScreen.userId)
                    },
                )
            }
        }
    }
}
