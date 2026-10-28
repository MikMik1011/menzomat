package st.misa.menzomat.card.presentation.card

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch
import okio.IOException
import org.koin.compose.viewmodel.koinViewModel
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.dump_copied
import st.misa.menzomat.shared.resources.dump_save_failed
import st.misa.menzomat.shared.resources.dump_saved
import st.misa.menzomat.ui.platform.LocalPlatformServices

@Composable
fun CardRoute(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val platform = LocalPlatformServices.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val copiedMessage = stringResource(Res.string.dump_copied)
    val savedMessage = stringResource(Res.string.dump_saved)
    val saveFailedMessage = stringResource(Res.string.dump_save_failed)
    // Not part of the effect loop below: the save dialog stops this activity, which would cancel that loop.
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    CardEffect.OpenNfcSettings -> platform.nfcSettings.open()
                    is CardEffect.ShareText -> platform.shareSheet.shareText(effect.text)
                    is CardEffect.CopyText -> {
                        platform.clipboard.copyText(effect.text)
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                    is CardEffect.SaveText -> scope.launch {
                        val message = try {
                            if (platform.fileSaver.saveText(effect.fileName, effect.text)) savedMessage else null
                        } catch (e: IOException) {
                            saveFailedMessage
                        }
                        message?.let { snackbarHostState.showSnackbar(it) }
                    }
                }
            }
        }
    }

    CardScreen(
        state = state,
        actions = viewModel,
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
