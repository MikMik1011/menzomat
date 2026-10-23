package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.ExtraField
import st.misa.menzomat.card.domain.model.ExtraValue
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.extra_medical_check
import st.misa.menzomat.shared.resources.extra_medical_check_none
import st.misa.menzomat.shared.resources.extra_transaction_counter
import st.misa.menzomat.ui.components.InfoCard
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.platform.LocalPlatformServices

/** The label of an extra the app knows, in the user's language. Other extras show the profile's own label. */
internal fun knownExtraLabel(key: String): StringResource? = when (key) {
    "medical-check" -> Res.string.extra_medical_check
    "transaction-counter" -> Res.string.extra_transaction_counter
    else -> null
}

@Composable
internal fun ExtrasSection(card: StudentCard) {
    if (card.extras.isEmpty()) return
    InfoCard {
        card.extras.forEach { InfoRow(extraLabel(it), extraValue(it)) }
    }
}

@Composable
private fun extraLabel(extra: ExtraField): String = knownExtraLabel(extra.key)?.let { stringResource(it) } ?: extra.label

@Composable
private fun extraValue(extra: ExtraField): String = when (val value = extra.value) {
    is ExtraValue.Text -> value.text
    is ExtraValue.Date -> LocalPlatformServices.current.dateFormatter.mediumDate(value.date)
    ExtraValue.Absent -> if (extra.key == "medical-check") stringResource(Res.string.extra_medical_check_none) else "–"
}
