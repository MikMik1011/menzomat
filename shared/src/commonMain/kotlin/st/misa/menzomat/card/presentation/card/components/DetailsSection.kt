package st.misa.menzomat.card.presentation.card.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.card_number
import st.misa.menzomat.shared.resources.card_valid_until
import st.misa.menzomat.shared.resources.issued_on
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.components.InfoCard
import st.misa.menzomat.ui.platform.LocalPlatformServices

@Composable
internal fun DetailsSection(card: StudentCard) {
    val validity = card.validity
    if (card.cardNumber == null && validity == null) return
    val dates = LocalPlatformServices.current.dateFormatter
    InfoCard {
        card.cardNumber?.let { InfoRow(stringResource(Res.string.card_number), it) }
        validity?.issuedOn?.let { InfoRow(stringResource(Res.string.issued_on), dates.mediumDate(it)) }
        validity?.cardValidUntil?.let { InfoRow(stringResource(Res.string.card_valid_until), dates.mediumDate(it)) }
    }
}
