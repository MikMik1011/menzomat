package st.misa.menzomat.ui.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The 8dp spacing system. */
@Immutable
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    /** Body content stops growing here on large windows. */
    val contentMaxWidth: Dp = 840.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

val MaterialTheme.brand: BrandColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBrandColors.current

fun Modifier.contentMaxWidth(max: Dp = Spacing().contentMaxWidth): Modifier = widthIn(max = max)
