package com.Rajath.aura.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.Rajath.aura.vm.MeditateViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.Rajath.aura.player.PlayerManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement

/**
 * Fullscreen player that shows a breathing visualizer (audio-agnostic).
 * Uses BreathingBackground so the rings "breathe" even if amplitude capture isn't available.
 *
 * Controls are wired to PlayerManager (assumes PlayerManager exists and is initialized).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenPlayerScreen(
    navController: NavController,
    vm: MeditateViewModel = viewModel()
) {
    // read session / playback state from PlayerManager (shared singleton)
    val session by PlayerManager.currentSession.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()

    // background: match other screens' calming gradient
    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary
        )
    )
    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(session?.title ?: "", color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large breathing visualizer centered and responsive.
                // isPlaying speeds animation slightly (passed down to BreathingBackground)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BreathingBackground(
                        modifier = Modifier,
                        size = 420.dp,   // large, adjust 360..480 if you like
                        rings = 5,
                        isPlaying = isPlaying
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Controls row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { PlayerManager.play(session ?: return@IconButton) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    IconButton(
                        onClick = { PlayerManager.pause() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    IconButton(
                        onClick = { PlayerManager.stop() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = session?.subtitle ?: "",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}