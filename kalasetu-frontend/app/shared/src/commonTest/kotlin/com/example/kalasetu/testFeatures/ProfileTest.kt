package com.example.kalasetu.testFeatures

import com.example.kalasetu.features.profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.TestResult
import kotlin.test.*
import kotlin.test.Test


class DisplayCountTest {

    @Test fun zero()                = assertEquals("0",      0.toDisplayCount())
    @Test fun belowThousand()       = assertEquals("999",    999.toDisplayCount())
    @Test fun exactlyOneThousand()  = assertEquals("1K",     1_000.toDisplayCount())
    @Test fun onePointOneThousand() = assertEquals("1.1K",   1_100.toDisplayCount())
    @Test fun twelvePointFourK()    = assertEquals("12.4K",  12_400.toDisplayCount())
    @Test fun justBelowMillion()    = assertEquals("999.9K", 999_999.toDisplayCount())
    @Test fun exactlyOneMillion()   = assertEquals("1M",     1_000_000.toDisplayCount())
    @Test fun onePointTwoMillion()  = assertEquals("1.2M",   1_200_000.toDisplayCount())
    @Test fun twoPointEightK()      = assertEquals("2.8K",   2_847.toDisplayCount())
}


class InitialsTest {

    @Test fun normalName()       = assertEquals("SA", "Sarah Anderson".toInitials())
    @Test fun multipleSpaces()   = assertEquals("SA", "Sarah   Anderson".toInitials())
    @Test fun hyphenatedName()   = assertEquals("SA", "Sarah-Anderson".toInitials())
    @Test fun singleName()       = assertEquals("S",  "Sarah".toInitials())
    @Test fun emptyString()      = assertEquals("",   "".toInitials())
    @Test fun blankString()      = assertEquals("",   "   ".toInitials())
    @Test fun lowercaseName()    = assertEquals("SA", "sarah anderson".toInitials())
    @Test fun threeWordName()    = assertEquals("SA", "Sarah Jane Anderson".toInitials()) // only takes 2
    @Test fun leadingTrailing()  = assertEquals("SA", "  Sarah Anderson  ".toInitials())
}

class ProfileModelTest {

    private val testProfile = Profile(
        id            = "123",
        name          = "Sarah Anderson",
        username      = "sarahart",
        location      = "San Francisco, CA",
        bio           = "Digital artist",
        followers     = 2847,
        following     = 892,
        artworksCount = 156,
        totalLikes    = 12400,
        email         = "sarah.anderson@email.com",
        isVerified    = true,
        skills        = listOf("Digital Art", "Illustration"),
        artworksImages = listOf("url1", "url2"),
        achievements  = listOf(
            Achievement("Top Creator 2024", "Recognized as top 1% creator", AchievementIcon.TOP_CREATOR),
        ),
    )

    @Test
    fun profile_defaultValues() {
        val empty = Profile()
        assertEquals("", empty.name)
        assertEquals("", empty.username)
        assertFalse(empty.isVerified)
        assertEquals(0, empty.followers)
        assertTrue(empty.achievements.isEmpty())
    }

    @Test
    fun profile_copyUpdatesCorrectly() {
        val updated = testProfile.copy(name = "Jane Doe", email = "jane@email.com")
        assertEquals("Jane Doe",        updated.name)
        assertEquals("jane@email.com",  updated.email)
        // unchanged fields preserved
        assertEquals("sarahart",        updated.username)
        assertEquals("San Francisco, CA", updated.location)
    }

    @Test
    fun profile_initialsFromName() {
        assertEquals("SA", testProfile.name.toInitials())
    }

    @Test
    fun profile_followersDisplayCount() {
        assertEquals("2.8K", testProfile.followers.toDisplayCount())
    }

    @Test
    fun profile_totalLikesDisplayCount() {
        assertEquals("12.4K", testProfile.totalLikes.toDisplayCount())
    }

    @Test
    fun achievement_hasCorrectIconType() {
        val ach = testProfile.achievements.first()
        assertEquals(AchievementIcon.TOP_CREATOR, ach.iconType)
    }
}


class ProfileUiStateTest {

    @Test
    fun uiState_defaultValues() {
        val state = ProfileUiState()
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertNull(state.error)
        assertEquals(ProfileTab.ACHIEVEMENTS, state.selectedTab)
    }

    @Test
    fun uiState_loadingState() {
        val state = ProfileUiState(isLoading = true)
        assertTrue(state.isLoading)
        assertNull(state.profile)
    }

    @Test
    fun uiState_errorState() {
        val state = ProfileUiState(error = "Network error")
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertEquals("Network error", state.error)
    }

    @Test
    fun uiState_successState() {
        val profile = Profile(id = "1", name = "Sarah")
        val state   = ProfileUiState(profile = profile)
        assertNotNull(state.profile)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun uiState_tabSelection() {
        val state = ProfileUiState(selectedTab = ProfileTab.POSTS)
        assertEquals(ProfileTab.POSTS, state.selectedTab)

        val updated = state.copy(selectedTab = ProfileTab.SKILLS)
        assertEquals(ProfileTab.SKILLS, updated.selectedTab)
    }

    @Test
    fun uiState_clearErrorOnSuccess() {
        val errorState   = ProfileUiState(error = "Failed")
        val successState = errorState.copy(
            profile = Profile(id = "1", name = "Sarah"),
            error   = null,
        )
        assertNull(successState.error)
        assertNotNull(successState.profile)
    }
}


class ProfileRepositoryTest {

    private val repository = ProfileRepository()

    @Test
    fun repository_fetchReturnsProfile(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertNotNull(profile)
    }

    @Test
    fun repository_fetchReturnsCorrectId(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertEquals("123", profile.id)
    }

    @Test
    fun repository_profileHasName(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertTrue(profile.name.isNotBlank())
    }

    @Test
    fun repository_profileHasEmail(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertTrue(profile.email.isNotBlank())
        assertTrue(profile.email.contains("@"))
    }

    @Test
    fun repository_profileHasAchievements(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertTrue(profile.achievements.isNotEmpty())
    }

    @Test
    fun repository_profileHasSkills(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertTrue(profile.skills.isNotEmpty())
    }

    @Test
    fun repository_profileHasArtworks(): TestResult = runTest {
        val profile = repository.fetchProfile("123")
        assertTrue(profile.artworksImages.isNotEmpty())
    }

    @Test
    fun repository_differentUserIds(): TestResult = runTest {
        val profile1 = repository.fetchProfile("111")
        val profile2 = repository.fetchProfile("222")
        // Both return a profile (mock data) — IDs are stored correctly
        assertEquals("111", profile1.id)
        assertEquals("222", profile2.id)
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePresenterTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeView : ProfileContract.View {
        val loadingStates  = mutableListOf<Boolean>()
        val profiles       = mutableListOf<Profile>()
        val errors         = mutableListOf<String>()

        override fun showLoading(isLoading: Boolean) { loadingStates.add(isLoading) }
        override fun showProfile(profile: Profile)   { profiles.add(profile) }
        override fun showError(message: String)      { errors.add(message) }
    }

    private class FakeRepository(
        private val shouldFail: Boolean = false,
    ) : ProfileRepository() {
        override suspend fun fetchProfile(userId: String): Profile {
            if (shouldFail) throw Exception("Network error")
            return Profile(
                id       = userId,
                name     = "Test User",
                username = "testuser",
                email    = "test@test.com",
            )
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun presenter_showsLoadingThenProfile(): TestResult = runTest {
        val view      = FakeView()
        val presenter = ProfilePresenter(FakeRepository())
        presenter.attachView(view)

        presenter.loadProfile("123")
        advanceUntilIdle()

        assertTrue(actual = view.loadingStates.contains(element = true), message = "Loading states should contain true")
        assertTrue(actual = view.loadingStates.contains(element = false), message = "Loading states should contain false")
        assertEquals(1, view.profiles.size)
        assertEquals("123", view.profiles.first().id)
        assertEquals("Test User", view.profiles.first().name)

        presenter.detach()
    }

    @Test
    fun presenter_showsErrorOnFailure(): TestResult = runTest {
        val view      = FakeView()
        val presenter = ProfilePresenter(FakeRepository(shouldFail = true))
        presenter.attachView(view)

        presenter.loadProfile("123")
        advanceUntilIdle()

        assertTrue(view.errors.isNotEmpty())
        assertEquals("Network error", view.errors.first())
        assertTrue(view.profiles.isEmpty())

        presenter.detach()
    }

    @Test
    fun presenter_stopsLoadingOnError(): TestResult = runTest {
        val view      = FakeView()
        val presenter = ProfilePresenter(FakeRepository(shouldFail = true))
        presenter.attachView(view)

        presenter.loadProfile("123")
        advanceUntilIdle()

        assertEquals(expected = false, actual = view.loadingStates.last())

        presenter.detach()
    }

    @Test
    fun presenter_doesNotCallViewAfterDetach(): TestResult = runTest {
        val view      = FakeView()
        val presenter = ProfilePresenter(FakeRepository())
        presenter.attachView(view)
        presenter.detach()

        presenter.loadProfile("123")
        advanceUntilIdle()

        assertTrue(view.profiles.isEmpty())
        assertTrue(view.loadingStates.isEmpty())
    }

    @Test
    fun presenter_tabSelectionDoesNotCrash(): TestResult = runTest {
        val presenter = ProfilePresenter(FakeRepository())
        presenter.onTabSelected(ProfileTab.POSTS)
        presenter.onTabSelected(ProfileTab.SKILLS)
        presenter.onTabSelected(ProfileTab.ACHIEVEMENTS)

        presenter.detach()
    }

    @Test
    fun presenter_detachClearsView(): TestResult = runTest {
        val view      = FakeView()
        val presenter = ProfilePresenter(FakeRepository())
        presenter.attachView(view)
        presenter.detach()
        presenter.detach()
    }
}

class ScreenNavigationTest {

    @Test
    fun screen_profileCarriesUserId() {
        val screen = com.example.kalasetu.navigation.Screen.Profile(userId = "abc123")
        assertEquals("abc123", screen.userId)
    }

    @Test
    fun screen_editProfileCarriesUserId() {
        val screen = com.example.kalasetu.navigation.Screen.EditProfile(userId = "abc123")
        assertEquals("abc123", screen.userId)
    }

    @Test
    fun screen_profileAndEditProfileAreDifferent() {
        val profile: com.example.kalasetu.navigation.Screen = com.example.kalasetu.navigation.Screen.Profile(userId = "1")
        val editProfile: com.example.kalasetu.navigation.Screen = com.example.kalasetu.navigation.Screen.EditProfile(userId = "1")
        assertNotEquals(profile, editProfile)
    }

    @Test
    fun screen_profileEqualityByUserId() {
        val a: com.example.kalasetu.navigation.Screen = com.example.kalasetu.navigation.Screen.Profile(userId = "1")
        val b: com.example.kalasetu.navigation.Screen = com.example.kalasetu.navigation.Screen.Profile(userId = "1")
        val c: com.example.kalasetu.navigation.Screen = com.example.kalasetu.navigation.Screen.Profile(userId = "2")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}