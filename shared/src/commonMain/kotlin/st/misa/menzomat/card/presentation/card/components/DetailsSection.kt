package st.misa.menzomat.card.presentation.card.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.card.presentation.card.maskNationalId
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.card_number
import st.misa.menzomat.shared.resources.card_valid_until
import st.misa.menzomat.shared.resources.issued_on
import st.misa.menzomat.shared.resources.national_id
import st.misa.menzomat.shared.resources.national_id_hide
import st.misa.menzomat.shared.resources.national_id_show
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.components.InfoCard
import st.misa.menzomat.ui.platform.LocalPlatformServices

@Composable
internal fun DetailsSection(card: StudentCard) {
    val validity = card.validity
    val nationalId = card.holder?.nationalId
    if (card.cardNumber == null && validity == null && nationalId == null) return
    val dates = LocalPlatformServices.current.dateFormatter
    InfoCard {
        card.cardNumber?.let { InfoRow(stringResource(Res.string.card_number), it) }
        validity?.issuedOn?.let { InfoRow(stringResource(Res.string.issued_on), dates.mediumDate(it)) }
        validity?.cardValidUntil?.let { InfoRow(stringResource(Res.string.card_valid_until), dates.mediumDate(it)) }
        nationalId?.let { NationalIdRow(it) }
    }
}

@Composable
private fun NationalIdRow(nationalId: String) {
    var revealed by rememberSaveable { mutableStateOf(false) }
    val shown = if (revealed) nationalId else maskNationalId(nationalId)
    InfoRow(
        label = stringResource(Res.string.national_id),
        value = shown,
        trailing = {
            IconButton(onClick = { revealed = !revealed }) {
                Icon(
                    imageVector = if (revealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = stringResource(if (revealed) Res.string.national_id_hide else Res.string.national_id_show),
                )
            }
        },
    )
}
