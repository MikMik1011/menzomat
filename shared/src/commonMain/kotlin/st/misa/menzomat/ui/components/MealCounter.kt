package st.misa.menzomat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import st.misa.menzomat.ui.theme.LocalReducedMotion

/** One big number with a caption. Reads as a single unit: "Ručak: 2". */
@Composable
fun MealCounter(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
) {
    val reducedMotion = LocalReducedMotion.current
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$label: $count" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                if (reducedMotion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                }
            },
            label = "meal count",
        ) { value ->
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = contentColor,
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}
