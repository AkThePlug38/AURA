package com.Rajath.aura.ui

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Rajath.aura.vm.MeditateViewModel
import com.Rajath.aura.vm.MeditationSession
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.Rajath.aura.audio.AudioVisualizerManager


/**
 * MeditateScreen:
 * - grid of full-width tiles (each tile fills column width)
 * - floating mini-player
 * - tapping expand navigates to fullscreen player route ("fullscreen_player")
 *
 * Expects MeditateViewModel with:
 *   val sessions: StateFlow<List<MeditationSession>>
 *   val currentSession: StateFlow<MeditationSession?>
 *   val isPlaying: StateFlow<Boolean>
 *   fun play(session: MeditationSession)
 *   fun pause()
 *   fun stop()
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditateScreen(
    uid: String,
    onBack: () -> Unit,
    vm: MeditateViewModel = viewModel(),
    navController: NavHostController
) {
    val ctx = LocalContext.current
    val sessions by vm.sessions.collectAsState()
    val current by vm.currentSession.collectAsState()
    val playing by vm.isPlaying.collectAsState()

    // breathing background gradient (same as other screens)
    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bg).padding(16.dp)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Meditate", color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = {
                            vm.stop()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                    .padding(bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // subtle decorative circle
                        val transition = rememberInfiniteTransition()
                        val pulse by transition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.08f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse)
                        )

                        Box(
                            modifier = Modifier
                                .size((140 * pulse).dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 28.dp, y = (-18).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        )

                        Column(modifier = Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center) {
                            Text("Relax and unwind", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Choose a calm session and tap play. The session will loop until you stop it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Explore Title
                Text("Explore", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                // Grid: two columns but tiles are full-width visually (we make each tile fill the column)
                val tiles = if (sessions.isEmpty()) defaultDemoSessions() else sessions
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1), // single column: make each tile full width
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(tiles, key = { it.id }) { s ->
                        MeditationGridTile(
                            session = s,
                            onClick = { vm.play(s) },
                            onExpand = {
                                // navigate to fullscreen player
                                navController.navigate("fullscreen_player")
                            }
                        )
                    }
                }
            }
        }

        // Floating player
        if (current != null) {
            FloatingPlayerMini(
                session = current!!,
                isPlaying = playing,
                onPlay = { vm.play(it) },
                onPause = { vm.pause() },
                onStop = { vm.stop() },
                onExpand = { navController.navigate("fullscreen_player") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
            )
        }
    }
}

/** Tile with expand icon and full-width look */
@Composable
private fun MeditationGridTile(session: MeditationSession, onClick: () -> Unit, onExpand: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(session.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                    Text("Loop", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onClick, modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/** Floating mini player */
@Composable
private fun FloatingPlayerMini(
    session: MeditationSession,
    isPlaying: Boolean,
    onPlay: (MeditationSession) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().height(76.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(session.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            }

            IconButton(onClick = { if (isPlaying) onPause() else onPlay(session) }, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary)) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onExpand, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Expand, contentDescription = "Fullscreen", tint = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = { onStop() }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** fallback demo sessions if vm.sessions is empty (useful while developing) */
private fun defaultDemoSessions(): List<MeditationSession> = listOf(
    MeditationSession(id = "breathing", title = "Breathing", subtitle = "Calm your breath — looped ambient.", source = "raw:meditation_breathing"),
    MeditationSession(id = "body_scan", title = "Body Scan", subtitle = "Full body relaxation — slow guided session.", source = "raw:meditation_body_scan"),
    MeditationSession(id = "sleep_short", title = "Sleep (short)", subtitle = "Short winddown session for easier sleep.", source = "raw:meditation_sleep_short")
)