package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Expressive Shape System
// Using larger corner radii for expressive design
val Shapes = Shapes(
    // Extra small: Chips, small buttons
    extraSmall = RoundedCornerShape(8.dp),
    
    // Small: Text fields, small cards
    small = RoundedCornerShape(12.dp),
    
    // Medium: Standard cards, buttons
    medium = RoundedCornerShape(16.dp),
    
    // Large: Large cards, FABs
    large = RoundedCornerShape(20.dp),
    
    // Extra large: Bottom sheets, dialogs, album art
    extraLarge = RoundedCornerShape(28.dp)
)





