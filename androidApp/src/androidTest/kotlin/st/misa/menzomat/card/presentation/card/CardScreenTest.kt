package st.misa.menzomat.card.presentation.card

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.Test
import st.misa.menzomat.R
import st.misa.menzomat.card.domain.model.CardReadError
import st.misa.menzomat.card.domain.model.Holder
import st.misa.menzomat.card.domain.model.Institution
import st.misa.menzomat.card.domain.model.MealBalance
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.core.domain.desfire.Aid
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.error_dismiss
import st.misa.menzomat.shared.resources.error_tag_lost
import st.misa.menzomat.shared.resources.meals_breakfast
import st.misa.menzomat.shared.resources.meals_dinner
import st.misa.menzomat.shared.resources.meals_left_to_buy
import st.misa.menzomat.shared.resources.meals_line
import st.misa.menzomat.shared.resources.meals_lunch
import st.misa.menzomat.shared.resources.nfc_disabled_action
import st.misa.menzomat.shared.resources.profile_auto
import st.misa.menzomat.shared.resources.profile_low_confidence
import st.misa.menzomat.shared.resources.prompt_title
import st.misa.menzomat.shared.resources.reading_title
import st.misa.menzomat.shared.resources.unrecognized_pick
import st.misa.menzomat.shared.resources.unrecognized_title
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.testPlatform
import st.misa.menzomat.ui.theme.MenzomatTheme
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private fun string(res: StringResource, vararg args: Any): String = runBlocking { getString(res, *args) }

    private val platform = testPlatform

    private val dump = CardDump("04AA", 0, listOf(AppDump(Aid(13))))
    private val uns = ProfileInfo("uns", "Univerzitet u Novom Sadu", "UNS")
    private val other = ProfileInfo("bg", "Univerzitet u Beogradu", "UB")
    private val candidates = listOf(ProfileCandidate(uns, 100), ProfileCandidate(other, 0))

    private val card = StudentCard(
        holder = Holder("Ana", "Anić"),
        cardNumber = "212503034",
        institution = Institution("Univerzitet u Novom Sadu", studyYear = 3, studentId = "209/22"),
        meals = MealBalance(breakfast = 1, lunch = 0, dinner = 2),
        cashBalanceRsd = 48,
    )

    private fun decoded(isAuto: Boolean = true, isLowConfidence: Boolean = false) = CardContent.Decoded(
        card = card,
        dump = dump,
        profile = uns,
        isAuto = isAuto,
        isLowConfidence = isLowConfidence,
        candidates = candidates,
    )

    private class RecordingActions : CardActions {
        val selections = mutableListOf<ProfileSelection>()
        var dismissed = 0
        var openedNfcSettings = 0
        override fun onSelectProfile(selection: ProfileSelection) { selections += selection }
        override fun onOpenNfcSettings() { openedNfcSettings++ }
        override fun onShareDump() = Unit
        override fun onCopyDump() = Unit
        override fun onDismissError() { dismissed++ }
    }

    private fun show(state: CardUiState, actions: CardActions = NoCardActions) {
        rule.setContent {
            CompositionLocalProvider(LocalPlatformServices provides platform) {
                MenzomatTheme {
                    CardScreen(state, actions, onOpenSettings = {}, snackbarHostState = SnackbarHostState())
                }
            }
        }
    }

    @Test
    fun decodedCardShowsEveryMealCounter() {
        show(CardUiState(isLoaded = true, content = decoded()))

        rule.onNodeWithContentDescription("${string(Res.string.meals_breakfast)}: 1").assertIsDisplayed()
        rule.onNodeWithContentDescription("${string(Res.string.meals_lunch)}: 0").assertIsDisplayed()
        rule.onNodeWithContentDescription("${string(Res.string.meals_dinner)}: 2").assertIsDisplayed()
        rule.onNodeWithText("Ana Anić").assertIsDisplayed()
    }

    @Test
    fun leftToBuyIsAnnouncedWithoutAVisibleSentence() {
        val withLeftToBuy = card.copy(mealsLeftToBuy = MealBalance(breakfast = 31, lunch = 28, dinner = 21))
        show(CardUiState(isLoaded = true, content = decoded().copy(card = withLeftToBuy)))

        val summary = string(Res.string.meals_line, 31, 28, 21)
        rule.onNodeWithContentDescription(string(Res.string.meals_left_to_buy, summary)).assertIsDisplayed()
    }

    @Test
    fun idleCardAsksToTapTheCard() {
        show(CardUiState(isLoaded = true))
        rule.onNodeWithText(string(Res.string.prompt_title)).assertIsDisplayed()
    }

    @Test
    fun readingStateReplacesThePrompt() {
        show(CardUiState(isLoaded = true, isReading = true))
        rule.onNodeWithText(string(Res.string.reading_title)).assertIsDisplayed()
        rule.onNodeWithText(string(Res.string.prompt_title)).assertDoesNotExist()
    }

    @Test
    fun choosingAProfileInThePickerIsReported() {
        val actions = RecordingActions()
        show(CardUiState(isLoaded = true, content = decoded()), actions)

        rule.onNodeWithText("UNS").performClick()
        rule.onNodeWithText(other.name).performClick()

        assertEquals(listOf<ProfileSelection>(ProfileSelection.Manual("bg")), actions.selections)
    }

    @Test
    fun pickerOffersAutomaticDetection() {
        val actions = RecordingActions()
        show(CardUiState(isLoaded = true, selection = ProfileSelection.Manual("bg"), content = decoded(isAuto = false)), actions)

        rule.onNodeWithText("UNS").performClick()
        rule.onNodeWithText(string(Res.string.profile_auto)).performClick()

        assertEquals(listOf<ProfileSelection>(ProfileSelection.Auto), actions.selections)
    }

    @Test
    fun forcedProfileThatDoesNotMatchIsFlagged() {
        show(CardUiState(isLoaded = true, content = decoded(isLowConfidence = true)))
        rule.onNodeWithText(string(Res.string.profile_low_confidence, uns.name)).assertIsDisplayed()
    }

    @Test
    fun unrecognizedCardOffersAProfileToTry() {
        show(CardUiState(isLoaded = true, content = CardContent.Unrecognized(dump, candidates)))
        rule.onNodeWithText(string(Res.string.unrecognized_title)).assertIsDisplayed()
        rule.onNodeWithText(string(Res.string.unrecognized_pick)).assertIsDisplayed()
    }

    @Test
    fun errorCanBeDismissed() {
        val actions = RecordingActions()
        show(CardUiState(isLoaded = true, error = CardReadError.TagLost), actions)

        rule.onNodeWithText(string(Res.string.error_tag_lost)).assertIsDisplayed()
        rule.onNodeWithText(string(Res.string.error_dismiss)).performClick()
        assertEquals(1, actions.dismissed)
    }

    @Test
    fun disabledNfcLinksToTheSystemSettings() {
        val actions = RecordingActions()
        show(CardUiState(isLoaded = true, nfcStatus = NfcStatus.Disabled), actions)

        rule.onNodeWithText(string(Res.string.nfc_disabled_action)).performClick()
        assertTrue(actions.openedNfcSettings == 1)
    }
}
