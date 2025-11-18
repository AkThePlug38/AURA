package com.Rajath.aura.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    uid: String,
    onOpenJournal: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenMeditate: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Read the Firebase displayName for the hero tile
    val firebaseName = FirebaseAuth.getInstance().currentUser?.displayName
    // fallback to your previous default
    val userName = firebaseName ?: "Rajath"

    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary
        )
    )

    val vm: com.Rajath.aura.vm.JournalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    LaunchedEffect(uid) { vm.observeRecent(uid) }
    val entries by vm.recentJournals.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            // --- HERO TILE (slightly larger) ---
            LargeHeroCard(
                userName = userName,
                onQuickJournal = onOpenJournal,
                onQuickMeditation = onOpenMeditate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // was 220 → better visual balance
            )

            Spacer(modifier = Modifier.height(20.dp))

            MoodChipsRow(onQuickJournal = onOpenJournal)

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Recent Mood Header ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent mood",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onOpenAnalytics) {
                    Text("View all", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Sparkline Tile (larger) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),     // was 140 → increased
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SparklineChart(
                        entries = entries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp) // slightly larger internal space
                    )
                }
            }

            // --- REDUCED Flex Spacer (pull Explore closer) ---
            Spacer(modifier = Modifier.weight(0.3f)) // was 1f → much smaller

            // --- Explore Header ---
            Text(
                "Explore",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCarousel(
                items = listOf(
                    FeatureTileData("Journal", "Write a thought", Icons.Default.Book, onOpenJournal),
                    FeatureTileData("History", "Recent entries", Icons.Default.History, onOpenHistory),
                    FeatureTileData("Analytics", "Mood trends", Icons.Default.BubbleChart, onOpenAnalytics),
                    FeatureTileData("Meditate", "Guided sessions", Icons.Default.MedicalServices, onOpenMeditate),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // slightly larger
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LargeHeroCard(
    userName: String,
    onQuickJournal: () -> Unit,
    modifier: Modifier = Modifier,
    onQuickMeditation: () -> Unit
) {
    // keep a subtle breathing animation for the decorative circle only
    val transition = rememberInfiniteTransition()
    val circlePulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    Card(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // decorative breathing circle — still animated
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-56).dp)
                    .scale(circlePulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)
                            )
                        )
                    )
                    .blur(28.dp)
            )

            // Content: use a Column with weights so items distribute evenly
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // top area (headline + subtext)
                Column(modifier = Modifier.weight(0.58f)) {
                    Text(
                        text = "Hi $userName!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Check in with yourself — write a few thoughts or record a quick clip.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // action row — no pulse on the card anymore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.42f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button(
                        onClick = onQuickJournal,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f)
                    ) {
                        Text(text = "Write in Journal", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = onQuickMeditation,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headset,
                            contentDescription = "Quick voice",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodChipsRow(
    onQuickJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = listOf(
        "😁" to "Great",
        "🙂" to "Good",
        "😐" to "Okay",
        "😔" to "Sad",
        "😡" to "Angry"
    )

    val scope = rememberCoroutineScope()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 0.dp, end = 8.dp),
        modifier = modifier
    ) {
        items(
            items = chips,
            key = { it.second }
        ) { (emoji, label) ->
            ElevatedSuggestionChip(
                emoji = emoji,
                label = label,
                onClick = { scope.launch { onQuickJournal() } }
            )
        }
    }
}

@Composable
private fun ElevatedSuggestionChip(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 100.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SparklineTile(
    entries: List<com.Rajath.aura.data.JournalEntry>,
    modifier: Modifier = Modifier
) {
    // compute a tiny stat: latest sentiment & count
    val latest = entries.lastOrNull()
    latest?.sentiment ?: "—"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        // Recent mood header (styled like Explore)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent mood",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

// Minimal Sparkline tile: only sparkline inside a full-width card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Center the sparkline so it looks clean and spacious
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Reuse your SparklineChart composable
                SparklineChart(entries = entries, modifier = Modifier.fillMaxWidth().height(86.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

private data class FeatureTileData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun FeatureCarousel(items: List<FeatureTileData>, modifier: Modifier = Modifier) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 6.dp),
        modifier = modifier
    ) {
        items(items, key = { it.title }) { item ->
            FeatureCard(item)
        }
    }
}

@Composable
private fun FeatureCard(data: FeatureTileData) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { data.onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = data.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Icon(imageVector = data.icon, contentDescription = data.title, tint = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
            )
        }
    }
}
