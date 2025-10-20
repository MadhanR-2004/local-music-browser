package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Interactive Button with Material 3 Expressive animations
 * Bounces slightly when pressed for tactile feedback
 */
@Composable
fun InteractiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(52.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            content()
        }
    } else {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(52.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            content()
        }
    }
}

/**
 * Interactive Outlined Button with Material 3 Expressive animations
 */
@Composable
fun InteractiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .height(52.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        content()
    }
}

/**
 * Interactive Icon Button with Material 3 Expressive animations
 */
@Composable
fun InteractiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        content()
    }
}

/**
 * Small animated equalizer indicator to show "Now Playing" state.
 */
@Composable
fun NowPlayingIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = 2.dp,
    barGap: Dp = 2.dp,
    maxBarHeight: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val h1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h3"
    )

    val inactiveAlpha = 0.45f
    val alpha = if (isActive) 1f else inactiveAlpha

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(barGap)
    ) {
        val h1Dp = (maxBarHeight * (if (isActive) h1 else 0.35f)).coerceAtLeast(2.dp)
        val h2Dp = (maxBarHeight * (if (isActive) h2 else 0.25f)).coerceAtLeast(2.dp)
        val h3Dp = (maxBarHeight * (if (isActive) h3 else 0.45f)).coerceAtLeast(2.dp)
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(h1Dp)
                .background(barColor.copy(alpha = alpha), shape = MaterialTheme.shapes.extraSmall)
        )
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(h2Dp)
                .background(barColor.copy(alpha = alpha), shape = MaterialTheme.shapes.extraSmall)
        )
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(h3Dp)
                .background(barColor.copy(alpha = alpha), shape = MaterialTheme.shapes.extraSmall)
        )
    }
}

/**
 * Interactive Filled Icon Button with Material 3 Expressive animations
 */
@Composable
fun InteractiveFilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )
    
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    ) {
        content()
    }
}

/**
 * Interactive Filter Chip with Material 3 Expressive animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.scale(scale),
        enabled = enabled,
        leadingIcon = leadingIcon,
        interactionSource = interactionSource
    )
}

/**
 * Interactive Card with Material 3 Expressive animations
 */
@Composable
fun InteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        content()
    }
}

/**
 * Interactive FAB with Material 3 Expressive animations
 */
@Composable
fun InteractiveFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        content()
    }
}


