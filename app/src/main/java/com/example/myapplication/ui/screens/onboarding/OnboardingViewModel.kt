package com.example.myapplication.ui.screens.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel for managing onboarding state
 */
class OnboardingViewModel : ViewModel() {
    var currentStep by mutableStateOf(0)
        private set
    
    var audioPermissionGranted by mutableStateOf(false)
        private set
    var notificationPermissionGranted by mutableStateOf(false)
        private set
    var foldersSelected by mutableStateOf(false)
        private set
    
    fun nextStep() {
        currentStep++
    }
    
    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
        }
    }
    
    fun onAudioPermissionResult(granted: Boolean) {
        audioPermissionGranted = granted
        if (granted) nextStep()
    }
    
    fun onNotificationPermissionResult(granted: Boolean) {
        notificationPermissionGranted = granted
        if (granted) nextStep()
    }
    
    fun onFoldersSelected(selected: Boolean) {
        foldersSelected = selected
    }
    
    fun completeOnboarding(context: Context) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()
    }
    
    fun isOnboardingComplete(): Boolean {
        return audioPermissionGranted && notificationPermissionGranted && foldersSelected
    }
}

