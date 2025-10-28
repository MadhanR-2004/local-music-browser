package com.example.myapplication.ui.screens.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.components.FolderCheckboxItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Folder Selection Screen - Multi-select music folders
 * Works exactly like onboarding folder selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectionScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    isOnboarding: Boolean = false
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    
    // Scan for folders that actually exist on the device
    var folderItems by remember {
        mutableStateOf<List<FolderItem>>(emptyList())
    }
    
    var isScanning by remember { mutableStateOf(true) }
    
    val selectedCount = folderItems.count { it.isSelected }
    
    // Load previously selected folders and scan for existing folders
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Get previously selected folders
            val previouslySelected = sharedPrefs.getStringSet("music_folder_paths", emptySet()) ?: emptySet()
            
            // Scan for existing folders
            val foundFolders = scanForMusicFolders()
            
            withContext(Dispatchers.Main) {
                // Only select folders that were previously selected during onboarding
                val mergedFolders = foundFolders.map { folder ->
                    folder.copy(isSelected = folder.path in previouslySelected)
                }
                
                folderItems = mergedFolders
                isScanning = false
                android.util.Log.d("FolderSelectionScreen", "Found ${foundFolders.size} existing folders, ${previouslySelected.size} previously selected")
            }
        }
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isOnboarding) "Select Music Folders" else "Music Folders"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Move buttons to top bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                folderItems = folderItems.map { it.copy(isSelected = false) }
                            }
                        ) {
                            Text("Clear All")
                        }
                        
                        FilledTonalButton(
                            onClick = {
                                // Get selected folder paths
                                val selectedPaths = folderItems
                                    .filter { it.isSelected }
                                    .map { it.path }
                                    .toSet()
                                
                                // Save folders
                                sharedPrefs.edit()
                                    .putStringSet("music_folder_paths", selectedPaths)
                                    .commit()
                                
                                android.util.Log.d("FolderSelectionScreen", "Saved ${selectedPaths.size} folders: $selectedPaths")
                                
                                if (isOnboarding) {
                                    // Complete onboarding
                                    sharedPrefs.edit()
                                        .putBoolean("onboarding_done", true)
                                        .apply()
                                    
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    // Show toast notification
                                    android.widget.Toast.makeText(
                                        context,
                                        "Rescanning library...",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    // Trigger WorkManager-based rescan (adds/removes diffs)
                                    try {
                                        val wm = androidx.work.WorkManager.getInstance(context.applicationContext)
                                        val request = androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication.worker.MusicScanWorker>()
                                            .build()
                                        wm.enqueue(request)
                                        android.util.Log.d("SettingsFolderSelection", "Enqueued MusicScanWorker for rescan")
                                    } catch (e: Exception) {
                                        android.util.Log.e("SettingsFolderSelection", "Failed to enqueue MusicScanWorker", e)
                                    }
                                    
                                    // Return to settings
                                    navController.navigateUp()
                                }
                            }
                        ) {
                            Text("Done ($selectedCount)")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Instructions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isOnboarding) "Choose Your Music" else "Manage Music Folders",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOnboarding) 
                            "Select folders containing your music. We'll scan them for audio files."
                        else 
                            "Add or remove folders containing your music files. Changes will trigger a library rescan.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "$selectedCount folder(s) selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Folder list with checkboxes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isScanning) {
                    // Show loading while scanning for folders
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Scanning for music folders...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (folderItems.isEmpty()) {
                    // No folders found
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No music folders found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Make sure you have audio files on your device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(folderItems.size) { index ->
                            val folder = folderItems[index]
                            FolderCheckboxItem(
                                folder = folder,
                                onCheckedChange = { checked ->
                                    folderItems = folderItems.toMutableList().also {
                                        it[index] = folder.copy(isSelected = checked)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

