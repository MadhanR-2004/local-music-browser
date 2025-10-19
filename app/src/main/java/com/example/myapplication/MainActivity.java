package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * DEPRECATED: This activity is no longer used.
 * The app now uses MainActivityCompose.kt for the UI.
 * This stub exists only to prevent build errors.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This activity is deprecated - all UI is now in MainActivityCompose
        finish();
    }
}
