package st.misa.menzomat.card.presentation.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import st.misa.menzomat.card.domain.model.NfcStatus
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.presentation.card.components.BetterMatchDialog
import st.misa.menzomat.card.presentation.card.components.DecodedCard
import st.misa.menzomat.card.presentation.card.components.DeleteDataDialog
import st.misa.menzomat.card.presentation.card.components.ErrorCard
import st.misa.menzomat.card.presentation.card.components.IdlePrompt
import st.misa.menzomat.card.presentation.card.components.NfcDisabledCard
import st.misa.menzomat.card.presentation.card.components.NfcUnavailableCard
import st.misa.menzomat.card.presentation.card.components.ProfilePicker
import st.misa.menzomat.card.presentation.card.components.UnrecognizedCard
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.action_delete_data
import st.misa.menzomat.shared.resources.action_settings
import st.misa.menzomat.shared.resources.card_title
import st.misa.menzomat.ui.theme.contentMaxWidth
import st.misa.menzomat.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScreen(
    state: CardUiState,
    actions: CardActions,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val isWide = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.card_title)) },
                actions = {
                    if (state.content != CardContent.Empty) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(Res.string.action_delete_data))
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(Res.string.action_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primaryContainer,
                    titleContentColor = colors.onPrimaryContainer,
                    actionIconContentColor = colors.onPrimaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (state.isLoaded) {
                Body(state, actions, isWide, onPickProfile = { pickerOpen = true })
            }
        }
    }

    if (confirmDelete) {
        DeleteDataDialog(
            onConfirm = {
                confirmDelete = false
                actions.onDeleteData()
            },
            onDismiss = { confirmDelete = false },
        )
    }

    (state.content as? CardContent.Decoded)?.betterMatch?.let { match ->
        BetterMatchDialog(
            profileName = match.profile.name,
            onSwitch = { actions.onSelectProfile(ProfileSelection.Auto) },
            onKeep = actions::onKeepProfile,
        )
    }

    if (pickerOpen) {
        val candidates = when (val content = state.content) {
            is CardContent.Decoded -> content.candidates
            is CardContent.Unrecognized -> content.candidates
            CardContent.Empty -> emptyList()
        }
        ProfilePicker(
            candidates = candidates,
            selection = state.selection,
            onSelect = actions::onSelectProfile,
            onDismiss = { pickerOpen = false },
            useDialog = isWide,
        )
    }
}

@Composable
private fun Body(
    state: CardUiState,
    actions: CardActions,
    isWide: Boolean,
    onPickProfile: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isReading && state.content != CardContent.Empty) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        Column(
            modifier = Modifier.contentMaxWidth().fillMaxWidth().padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            state.error?.let { ErrorCard(it, actions::onDismissError) }
            when (state.nfcStatus) {
                NfcStatus.Unavailable -> NfcUnavailableCard()
                NfcStatus.Disabled -> NfcDisabledCard(actions::onOpenNfcSettings)
                NfcStatus.Ready -> Unit
            }
            when (val content = state.content) {
                CardContent.Empty -> if (state.nfcStatus == NfcStatus.Ready) IdlePrompt(state.isReading)
                is CardContent.Decoded -> DecodedCard(content, actions, isWide)
                is CardContent.Unrecognized -> UnrecognizedCard(content, actions, onPickProfile)
            }
        }
    }
}
