package com.spendsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendsense.ui.theme.BluePrimary

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = Easing { fraction ->
                val t = fraction - 1.0f
                t * t * ((2.0f + 1.0f) * t + 2.0f) + 1.0f
            }
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = BluePrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SpendSense",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary,
                modifier = Modifier.scale(scale)
            )
            Text(
                text = "Secure. Smart. Simple.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.scale(scale)
            )
        }
    }
}
