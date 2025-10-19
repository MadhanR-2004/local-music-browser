package com.example.myapplication.ui.screens.onboarding

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.components.FolderCheckboxItem

/**
 * Multi-step onboarding screen with permissions and folder selection
 */
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Check if onboarding is already done
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_done", false)) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.FolderSelection.route) { inclusive = true }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            StepIndicator(
                currentStep = viewModel.currentStep,
                totalSteps = 4,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated content for each step
            AnimatedContent(
                targetState = viewModel.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(onNext = { viewModel.nextStep() })
                    1 -> AudioPermissionStep(
                        onPermissionResult = { granted ->
                            viewModel.onAudioPermissionResult(granted)
                        }
                    )
                    2 -> NotificationPermissionStep(
                        onPermissionResult = { granted ->
                            viewModel.onNotificationPermissionResult(granted)
                        }
                    )
                    3 -> FolderSelectionStep(
                        navController = navController,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isCompleted = index < currentStep
            
            Box(
                modifier = Modifier
                    .size(if (isActive) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    .animateContentSize()
            )
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Welcome to Music Player",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your personal music companion",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem(
                    icon = Icons.Default.Folder,
                    title = "Local Music Library",
                    description = "Access your music files stored on device"
                )
                FeatureItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Smart Playback",
                    description = "Enjoy seamless music playback"
                )
                FeatureItem(
                    icon = Icons.Default.Palette,
                    title = "Beautiful Design",
                    description = "Material You design with dynamic colors"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        FilledTonalButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, "Next")
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AudioPermissionStep(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
    
    PermissionStepContent(
        icon = Icons.Default.MusicNote,
        title = "Access Your Music",
        description = "We need permission to read audio files from your device storage. This allows the app to scan and play your music collection.",
        stepNumber = "Step 1 of 3",
        buttonText = "Grant Permission",
        onButtonClick = {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
    )
}

@Composable
fun NotificationPermissionStep(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
    
    // Skip if Android version < 13 (Tiramisu)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onPermissionResult(true) // Auto-grant for older versions
        }
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionStepContent(
            icon = Icons.Default.Notifications,
            title = "Music Notifications",
            description = "Allow notifications to control playback from your lock screen and notification shade. You can pause, skip, and see what's playing.",
            stepNumber = "Step 2 of 3",
            buttonText = "Enable Notifications",
            onButtonClick = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onSkip = {
                onPermissionResult(false)
            }
        )
    }
}

data class FolderItem(
    val name: String,
    val path: String,
    var isSelected: Boolean = true
)

@Composable
fun FolderSelectionStep(
    navController: NavController,
    viewModel: OnboardingViewModel,
    context: Context
) {
    // Scan for folders that actually exist on the device
    var folderItems by remember {
        mutableStateOf<List<FolderItem>>(emptyList())
    }
    
    var isScanning by remember { mutableStateOf(true) }
    
    val selectedCount = folderItems.count { it.isSelected }
    
    // Scan for existing folders on first composition
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val foundFolders = scanForMusicFolders()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                folderItems = foundFolders
                isScanning = false
                android.util.Log.d("FolderSelection", "Found ${foundFolders.size} existing folders")
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Select Music Folders",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Step 3 of 3",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choose folders containing your music files. We'll scan them to build your library.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                androidx.compose.foundation.lazy.LazyColumn(
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                if (selectedCount > 0) {
                    // Get selected folder paths
                    val selectedPaths = folderItems
                        .filter { it.isSelected }
                        .map { it.path }
                        .toSet()
                    
                    // Save folders
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("music_folder_paths", selectedPaths)
                        .apply()
                    
                    android.util.Log.d("Onboarding", "Saved ${selectedPaths.size} folders: $selectedPaths")
                    
                    viewModel.onFoldersSelected(true)
                    viewModel.completeOnboarding(context)
                    
                    // Show loading dialog while scanning
                    android.widget.Toast.makeText(
                        context,
                        "Scanning ${selectedPaths.size} folders...",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    // Trigger music library scan in background
                    android.os.AsyncTask.SERIAL_EXECUTOR.execute {
                        android.util.Log.d("Onboarding", "Starting music scan of ${selectedPaths.size} folders...")
                        com.example.myapplication.InitialLoadInitializer.runAsync(
                            context.applicationContext as android.app.Application,
                            //true
                        )
                        
                        // Wait for scan to complete (give it a moment)
                        Thread.sleep(2000)
                        
                        android.util.Log.d("Onboarding", "Scan initiated, navigating to home...")
                    }
                    
                    // Navigate to home immediately (scan will continue in background)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedCount > 0
        ) {
            Text("Finish Setup ($selectedCount folders)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Check, "Done")
        }
    }
}

@Composable
fun PermissionStepContent(
    icon: ImageVector,
    title: String,
    description: String,
    stepNumber: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onSkip: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = stepNumber,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(buttonText, style = MaterialTheme.typography.titleMedium)
        }
        
        if (onSkip != null) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now")
            }
        }
    }
}

