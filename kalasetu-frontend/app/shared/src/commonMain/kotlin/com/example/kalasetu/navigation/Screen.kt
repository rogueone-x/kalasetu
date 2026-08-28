package com.example.kalasetu.navigation

sealed class Screen {
    // Auth Screens
    data object AuthSignup : Screen()
    data object AuthOtp : Screen()
    data object AuthLogin : Screen()

    // Common Onboarding Screens (Welcome, BasicInfo, Location, and Done Screens)
    data object OnboardingWelcome : Screen()
    data object OnboardingBasicInfo : Screen()
    data object OnboardingLocation : Screen()
    data object OnboardingDone : Screen()

    // Role Specific Onboarding Screens (Artist - Experience Screen, Organizer - Type Screen, Intent Screen, Audience - Interests Screen)
    data object ArtistExperience : Screen()
    data object OrganizerType : Screen()
    data object OrganizerIntent : Screen()
    data object AudienceInterests : Screen()

}