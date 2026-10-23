package st.misa.menzomat.card.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import st.misa.menzomat.card.domain.model.ProfileSelection
import st.misa.menzomat.card.domain.model.ThemeMode
import st.misa.menzomat.shared.resources.Res
import st.misa.menzomat.shared.resources.about_license
import st.misa.menzomat.shared.resources.about_source
import st.misa.menzomat.shared.resources.about_version
import st.misa.menzomat.shared.resources.settings_about_section
import st.misa.menzomat.shared.resources.settings_back
import st.misa.menzomat.shared.resources.settings_dynamic_color
import st.misa.menzomat.shared.resources.settings_profile_auto
import st.misa.menzomat.shared.resources.settings_profile_section
import st.misa.menzomat.shared.resources.settings_theme_dark
import st.misa.menzomat.shared.resources.settings_theme_light
import st.misa.menzomat.shared.resources.settings_theme_section
import st.misa.menzomat.shared.resources.settings_theme_system
import st.misa.menzomat.shared.resources.settings_title
import st.misa.menzomat.ui.components.ChoiceRow
import st.misa.menzomat.ui.components.InfoRow
import st.misa.menzomat.ui.components.SectionHeader
import st.misa.menzomat.ui.theme.contentMaxWidth
import st.misa.menzomat.ui.platform.LocalPlatformServices
import st.misa.menzomat.ui.theme.spacing

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state, viewModel, onBack, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    actions: SettingsActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(Res.string.settings_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primaryContainer,
                    titleContentColor = colors.onPrimaryContainer,
                    navigationIconContentColor = colors.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .contentMaxWidth()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                ProfileSection(state, actions)
                ThemeSection(state, actions)
                AboutSection()
            }
        }
    }
}

@Composable
private fun ProfileSection(state: SettingsUiState, actions: SettingsActions) {
    val selection = state.settings.selection
    SectionTitle(stringResource(Res.string.settings_profile_section))
    ChoiceRow(
        title = stringResource(Res.string.settings_profile_auto),
        selected = selection is ProfileSelection.Auto,
        onClick = { actions.onSelectProfile(ProfileSelection.Auto) },
    )
    state.profiles.forEach { profile ->
        ChoiceRow(
            title = profile.name,
            selected = (selection as? ProfileSelection.Manual)?.profileId == profile.id,
            onClick = { actions.onSelectProfile(ProfileSelection.Manual(profile.id)) },
        )
    }
}

@Composable
private fun ThemeSection(state: SettingsUiState, actions: SettingsActions) {
    SectionTitle(stringResource(Res.string.settings_theme_section))
    val modes = listOf(
        ThemeMode.System to Res.string.settings_theme_system,
        ThemeMode.Light to Res.string.settings_theme_light,
        ThemeMode.Dark to Res.string.settings_theme_dark,
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium)) {
        modes.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = state.settings.themeMode == mode,
                onClick = { actions.onSelectTheme(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
            ) { Text(stringResource(label)) }
        }
    }
    if (LocalPlatformServices.current.theme.supportsDynamicColor) {
        ListItem(
            modifier = Modifier.toggleable(
                value = state.settings.useDynamicColor,
                role = Role.Switch,
                onValueChange = actions::onToggleDynamicColor,
            ),
            headlineContent = { Text(stringResource(Res.string.settings_dynamic_color)) },
            trailingContent = { Switch(checked = state.settings.useDynamicColor, onCheckedChange = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun AboutSection() {
    SectionTitle(stringResource(Res.string.settings_about_section))
    val version = LocalPlatformServices.current.appInfo.versionName
    if (version.isNotBlank()) InfoRow(stringResource(Res.string.about_version), version)
    LinkRow(stringResource(Res.string.about_source), SOURCE_URL)
    LinkRow(stringResource(Res.string.about_license), LICENSE_URL)
}

/** A row that opens [url] outside the app; the trailing icon says so. */
@Composable
private fun LinkRow(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    ListItem(
        modifier = Modifier.clickable(role = Role.Button) { runCatching { uriHandler.openUri(url) } },
        headlineContent = { Text(label) },
        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

private const val SOURCE_URL = "https://github.com/MikMik1011/menzomat"
private const val LICENSE_URL = "$SOURCE_URL/blob/main/LICENSE"

@Composable
private fun SectionTitle(text: String) {
    SectionHeader(text, Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small))
}
