package com.Rajath.aura.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Rajath.aura.vm.JournalEvent
import com.Rajath.aura.vm.JournalUiState
import com.Rajath.aura.vm.JournalViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    vm: JournalViewModel = viewModel(),
    userId: String? = null
) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboard = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    val charLimit = 800

    // effective UID: prefer provided userId, otherwise fall back to FirebaseAuth
    val effectiveUid = userId ?: FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(effectiveUid) {
        Log.d("AURA-UI", "JournalScreen launched with effectiveUid=$effectiveUid (passed userId=$userId, authUid=${FirebaseAuth.getInstance().currentUser?.uid})")
    }

    // listen to one-off events
    LaunchedEffect(vm) {
        vm.events.collectLatest { ev ->
            when (ev) {
                is JournalEvent.Saved -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Saved")
                    }
                    vm.reset()
                    text = ""
                }

                is JournalEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: ${ev.message}")
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    // subtle gradient: surface -> primary
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .padding(20.dp)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Journal",
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Input card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)),
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Write how you feel",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "${text.length}/$charLimit",
                                color = if (text.length > charLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.75f
                                ),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = text,
                            onValueChange = {
                                if (it.length <= charLimit) text = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = {
                                Text(
                                    "Today I felt...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            maxLines = 8,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                    if (text.isNotBlank() && uiState !is JournalUiState.Loading) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (effectiveUid == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please sign in to save journals.")
                                            }
                                        } else {
                                            vm.analyzeAndSave(effectiveUid, text)
                                        }
                                    }
                                }
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val isLoading = uiState is JournalUiState.Loading
                            Button(
                                onClick = {
                                    keyboard?.hide()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (effectiveUid == null) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please sign in to save journals.")
                                        }
                                    } else {
                                        vm.analyzeAndSave(effectiveUid, text)
                                    }
                                },
                                enabled = text.isNotBlank() && uiState !is JournalUiState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Analyzing...", color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfied,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Analyze Mood", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Result card
                AnimatedVisibility(
                    visible = uiState is JournalUiState.Success,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val success = uiState as? JournalUiState.Success
                    val score = success?.score ?: 0f
                    val sentiment = success?.sentiment ?: "Unknown"
                    val animated = animateFloatAsState(targetValue = score)

                    Surface(
                        tonalElevation = 6.dp,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .shadow(10.dp, RoundedCornerShape(14.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mood result",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "$sentiment",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = when (sentiment) {
                                        "Positive" -> MaterialTheme.colorScheme.secondary
                                        "Neutral" -> MaterialTheme.colorScheme.primary
                                        "Negative" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Confidence: ${"%.3f".format(score)}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                val pct = animated.value
                                CircularProgressIndicator(
                                    progress = pct,
                                    modifier = Modifier.size(76.dp),
                                    strokeWidth = 6.dp,
                                    color = when {
                                        pct >= 0.66f -> MaterialTheme.colorScheme.secondary
                                        pct >= 0.33f -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                                Text(
                                    text = "${(pct * 100).toInt()}%",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Tip: Write freely — the model understands short and long entries.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}