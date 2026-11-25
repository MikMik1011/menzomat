package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import st.misa.menzomat.card.presentation.card.CardActions
import st.misa.menzomat.card.presentation.card.CardContent
import st.misa.menzomat.ui.components.PairRow
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun DecodedCard(content: CardContent.Decoded, actions: CardActions, isWide: Boolean) {
    val spacing = MaterialTheme.spacing
    val card = content.card
    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        HolderHeader(card)
        ExpiryBanner(card.validity)
        if (isWide) {
            if (content.isLowConfidence) LowConfidenceNote(content.profile.name)
            PairRow(spacing.medium) {
                MealsHero(card)
                DetailsSection(card)
            }
            PairRow(spacing.medium) {
                AccountSection(card)
                ExtrasSection(card)
            }
        } else {
            MealsHero(card)
            AccountSection(card)
            if (content.isLowConfidence) LowConfidenceNote(content.profile.name)
            DetailsSection(card)
            ExtrasSection(card)
        }
        WarningsSection(card)
        RawSection(content.dump, actions, initiallyExpanded = false)
    }
}
