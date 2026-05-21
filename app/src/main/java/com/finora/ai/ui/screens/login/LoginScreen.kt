package com.finora.ai.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════
// Login Screen — Multi-user simulation
// ═══════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    viewModel: FinoraViewModel,
    onLoginSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loginBg")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
        ),
        label = "bgOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(InkBlack, JetBlack, DarkTeal.copy(alpha = 0.3f))
                )
            ),
    ) {
        // Ambient floating orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val orbs = listOf(
                Triple(0.2f, 0.3f, MintLeaf),
                Triple(0.8f, 0.2f, Turquoise),
                Triple(0.6f, 0.7f, PearlAqua),
                Triple(0.15f, 0.8f, DarkTeal),
            )
            orbs.forEachIndexed { index, (xFraction, yFraction, color) ->
                val x = size.width * xFraction + sin(bgOffset + index * 1.5f) * 30f
                val y = size.height * yFraction + sin(bgOffset * 0.7f + index) * 20f
                drawCircle(
                    color = color.copy(alpha = 0.08f),
                    radius = 120f + sin(bgOffset + index) * 30f,
                    center = Offset(x, y)
                )
            }
        }

        // Main content
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MintLeaf, Turquoise)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "F",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = InkBlack,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to FinoraAI",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = DarkTextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your intelligent CFO companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextSecondary,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // ── Glass Card ──────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null, tint = Turquoise)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintLeaf,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = MintLeaf,
                                cursorColor = MintLeaf,
                                focusedTextColor = DarkTextPrimary,
                                unfocusedTextColor = DarkTextSecondary,
                            ),
                            singleLine = true,
                        )

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Turquoise)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = DarkTextTertiary,
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintLeaf,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = MintLeaf,
                                cursorColor = MintLeaf,
                                focusedTextColor = DarkTextPrimary,
                                unfocusedTextColor = DarkTextSecondary,
                            ),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Login button
                        Button(
                            onClick = {
                                isLoading = true
                                // Simulate multi-user session
                                val testUsers = listOf("taha@finora.ai", "founder@demo.pk", "auditor@check.com")
                                
                                // In a real app, this would set a state in the ViewModel
                                // For the demo, we set the user email property we just added
                                try {
                                    val setter = viewModel::class.java.getMethod("setUserEmail", String::class.java)
                                    setter.invoke(viewModel, if (email in testUsers) email else "taha@finora.ai")
                                } catch (e: Exception) {
                                    // Fallback if public setter isn't generated or named differently by Kotlin
                                }

                                onLoginSuccess()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintLeaf,
                                contentColor = InkBlack,
                            ),
                            enabled = !isLoading,
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = InkBlack,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Divider(modifier = Modifier.weight(1f), color = DarkBorder)
                    Text(
                        text = "  or continue with  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextTertiary,
                    )
                    Divider(modifier = Modifier.weight(1f), color = DarkBorder)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Google Sign-In button
                OutlinedButton(
                    onClick = {
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(MintLeaf.copy(alpha = 0.5f), Turquoise.copy(alpha = 0.5f))
                        ),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DarkTextPrimary,
                    ),
                ) {
                    Text(
                        text = "🔑  Sign in with Google",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "By continuing, you agree to FinoraAI's\nTerms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
