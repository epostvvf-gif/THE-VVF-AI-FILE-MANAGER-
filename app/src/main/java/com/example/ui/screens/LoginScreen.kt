package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Montserrat
import com.example.ui.theme.NotoSansDevanagari
import com.example.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        authViewModel.handleGoogleSignInResult(
            context = context,
            data = result.data,
            onSuccess = {
                isLoading = false
                onLoginSuccess()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C20),
                        Color(0xFF15102A),
                        Color(0xFF1E1435)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background glowing circular gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFFF7A00).copy(alpha = 0.08f),
                radius = 350.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(0f, 0f)
            )
            drawCircle(
                color = Color(0xFF6200EE).copy(alpha = 0.12f),
                radius = 450.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width, size.height)
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Futuristic Glowing Icon Container
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF7A00),
                                    Color(0xFFFFB347)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderSpecial,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Title
                Text(
                    text = "VVF SMART FILE\nMANAGER ULTRA",
                    fontFamily = NotoSansDevanagari,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "AI Storage & Copilot Companion",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Authentication Options Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Secure Unified Access",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Google OAuth Button
                        OAuthButton(
                            text = "Continue with Google",
                            iconContent = { GoogleIcon() },
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val intent = authViewModel.getGoogleSignInIntent(context)
                                    googleSignInLauncher.launch(intent)
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Google initialization failed: ${e.localizedMessage}"
                                }
                            },
                            testTag = "google_login_button"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Microsoft OAuth Button
                        OAuthButton(
                            text = "Sign in with Microsoft",
                            iconContent = { MicrosoftIcon() },
                            onClick = {
                                if (activity != null) {
                                    isLoading = true
                                    errorMessage = null
                                    authViewModel.signInWithMicrosoft(
                                        activity = activity,
                                        onSuccess = {
                                            isLoading = false
                                            onLoginSuccess()
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            errorMessage = error
                                        }
                                    )
                                } else {
                                    errorMessage = "Activity Context unavailable."
                                }
                            },
                            testTag = "microsoft_login_button"
                        )
                    }
                }

                // Error Message Panel
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        fontFamily = Montserrat,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Loader
                if (isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        color = Color(0xFFFF7A00),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Footer Cryptography statement
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Safe Lock",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Keystore encryption & secure tokens applied",
                        fontFamily = Montserrat,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun OAuthButton(
    text: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(24.dp)) {
            iconContent()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color(0xFF1E1435)
        )
    }
}

@Composable
fun GoogleIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        // Render Google OAuth colored elements inside custom canvas for flawless vector compatibility
        // Red sector
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Yellow sector
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Green sector
        drawArc(
            color = Color(0xFF34A853),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Blue sector
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -135f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Central cutout
        drawCircle(
            color = Color.White,
            radius = width * 0.35f
        )
    }
}

@Composable
fun MicrosoftIcon() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF25022)))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF7FBA00)))
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF00A4EF)))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFB900)))
        }
    }
}
