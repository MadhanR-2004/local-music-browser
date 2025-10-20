package com.example.myapplication.ui.screens.settings
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.Context

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen - Complete app preferences
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val folderCount by viewModel.selectedFolderCount.collectAsState()
    
    // Folder picker launcher
    val context = LocalContext.current
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Persist permission
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // Save to SharedPreferences
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val folders = prefs.getStringSet("music_folder_paths", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            folders.add(uri.toString())
            prefs.edit().putStringSet("music_folder_paths", folders).apply()
            // Update ViewModel
            viewModel.loadSettings()
        }
    }

    // Set ViewModel callback for launching picker
    LaunchedEffect(Unit) {
        viewModel.onLaunchFolderPicker = {
            folderPickerLauncher.launch(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader("Appearance")
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Palette,
                    title = "Dynamic Colors",
                    subtitle = "Use colors from your wallpaper",
                    checked = useDynamicColors,
                    onCheckedChange = { viewModel.toggleDynamicColors() }
                )
            }
            
            // Library Section
            item {
                SettingsSectionHeader("Library")
            }
            
            item {
                SettingsClickableItem(
                    icon = Icons.Default.Folder,
                    title = "Music Folders",
                    subtitle = "$folderCount folders selected",
                    onClick = {
                        // Launch SAF folder picker, similar to onboarding
                        viewModel.launchFolderPicker()
                    }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.Refresh,
                    title = "Rescan Library",
                    subtitle = "Scan for new music files",
                    onClick = {
                        // Trigger WorkManager scan
                        viewModel.startBackgroundRescan()
                    }
                )
            }
            
            // Playback Section
            item {
                SettingsSectionHeader("Playback")
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.GraphicEq,
                    title = "Gapless Playback",
                    subtitle = "Seamless transitions between tracks",
                    checked = gaplessPlayback,
                    onCheckedChange = { viewModel.toggleGaplessPlayback() }
                )
            }
            
            item {
                SettingsSliderItem(
                    icon = Icons.Default.Shuffle,
                    title = "Crossfade Duration",
                    subtitle = "$crossfadeDuration seconds",
                    value = crossfadeDuration.toFloat(),
                    valueRange = 0f..10f,
                    onValueChange = { viewModel.setCrossfadeDuration(it.toInt()) }
                )
            }
            
            // Storage Section
            item {
                SettingsSectionHeader("Storage")
            }
            
            item {
                SettingsClickableItem(
                    icon = Icons.Default.CleaningServices,
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    onClick = { viewModel.clearCache() }
                )
            }
            
            // About Section
            item {
                SettingsSectionHeader("About")
            }
            
            item {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Beta)",
                    onClick = { }
                )
            }
            
            item {
                SettingsClickableItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsSliderItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}
