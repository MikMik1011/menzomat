package st.misa.menzomat.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import st.misa.menzomat.ui.theme.LocalReducedMotion
import st.misa.menzomat.ui.theme.spacing

/** The "tap your card" hero: a pulsing NFC glyph, or the expressive loading indicator while a read runs. The glyph and the title say it all. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NfcPrompt(
    title: String,
    isReading: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            if (isReading) {
                LoadingIndicator(modifier = Modifier.size(72.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                PulsingNfcIcon()
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PulsingNfcIcon() {
    val scale = if (LocalReducedMotion.current) {
        1f
    } else {
        rememberInfiniteTransition(label = "nfc pulse").animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 1_200), RepeatMode.Reverse),
            label = "nfc pulse scale",
        ).value
    }
    Icon(
        imageVector = Icons.Rounded.Nfc,
        contentDescription = null,
        modifier = Modifier.size(72.dp).scale(scale),
    )
}
