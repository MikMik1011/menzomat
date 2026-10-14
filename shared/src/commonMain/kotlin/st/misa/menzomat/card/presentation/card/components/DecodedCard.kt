package st.misa.menzomat.card.presentation.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import st.misa.menzomat.card.presentation.card.CardActions
import st.misa.menzomat.card.presentation.card.CardContent
import st.misa.menzomat.ui.theme.spacing

@Composable
internal fun DecodedCard(content: CardContent.Decoded, actions: CardActions, isWide: Boolean) {
    val spacing = MaterialTheme.spacing
    val card = content.card
    val primary: @Composable ColumnScope.() -> Unit = {
        HolderHeader(card)
        MealsHero(card)
        AccountSection(card)
    }
    val secondary: @Composable ColumnScope.() -> Unit = {
        if (content.isLowConfidence) LowConfidenceNote(content.profile.name)
        DetailsSection(card)
        ExtrasSection(card)
        WarningsSection(card)
        RawSection(content.dump, actions, initiallyExpanded = false)
    }
    if (isWide) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.medium), content = primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.medium), content = secondary)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            primary()
            secondary()
        }
    }
}
