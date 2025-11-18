package com.Rajath.aura.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.Rajath.aura.auth.AuthViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardActions
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToNameEntry: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Derived loading flag
    val isLoading = uiState is com.Rajath.aura.auth.AuthState.Loading

    // Unified error message: prefer localError (validation), then viewmodel error
    val viewModelError = (uiState as? com.Rajath.aura.auth.AuthState.Error)?.message
    val shownError = localError ?: viewModelError

    // background gradient
    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surface
        )
    )

    // Helper: validate email locally
    fun isEmailValid(e: String) = e.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(e).matches()

    // Helper: perform auth action (sign up or sign in). This centralizes the call and prevents duplication.
    fun performAuthAction() {
        // clear previous local validation errors
        localError = null

        // basic client-side validation
        if (!isEmailValid(email)) {
            localError = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            localError = "Password must be at least 6 characters."
            return
        }

        // Prevent double-call if already loading
        if (isLoading) return

        if (isSignUp) {
            authViewModel.signUpWithEmail(email.trim(), password) { success, info ->
                if (success) {
                    // After success, decide route by reloading user (centralized decision)
                    val auth = FirebaseAuth.getInstance()
                    auth.currentUser?.reload()?.addOnCompleteListener {
                        val displayName = auth.currentUser?.displayName
                        if (displayName.isNullOrBlank()) onNavigateToNameEntry() else onNavigateToHome()
                    } ?: onNavigateToNameEntry()
                } else {
                    // viewmodel will also set uiState error; we avoid duplicating localError here
                    // but if you want to show the callback info explicitly, set localError = info
                    // localError = info
                }
            }
        } else {
            authViewModel.signInWithEmail(email.trim(), password) { success, info ->
                if (success) {
                    val auth = FirebaseAuth.getInstance()
                    auth.currentUser?.reload()?.addOnCompleteListener {
                        val displayName = auth.currentUser?.displayName
                        if (displayName.isNullOrBlank()) onNavigateToNameEntry() else onNavigateToHome()
                    } ?: onNavigateToHome()
                } else {
                    // see note above re: errors
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp)
    ) {
        // Decorative circle at top-right
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-56).dp)
                .scale(1.05f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome hero
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in to continue your journaling and mood tracking.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localError = null },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localError = null },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                performAuthAction()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            performAuthAction()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading // disable while loading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Please wait")
                        } else {
                            Text(text = if (isSignUp) "Create account" else "Sign in")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { isSignUp = !isSignUp }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = if (isSignUp) "Already have an account? Sign in" else "Create an account", textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // single place to show any error (either local validation or viewmodel)
                    shownError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // small footer note
            Text(
                text = "Your journals are private — saved securely to your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}