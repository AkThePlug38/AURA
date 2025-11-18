package com.Rajath.aura.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Rajath.aura.vm.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    uid: String,
    onBack: () -> Unit,
    vm: JournalViewModel = viewModel()
) {
    BackHandler { onBack() }

    // same gradient as JournalScreen / Home
    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Recent Journals", color = MaterialTheme.colorScheme.onSurface) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f))
        )

        // Content area — keep padding so it matches JournalScreen layout
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Reuse the JournalList composable, which contains a LazyColumn
            // It will be scrollable and its Cards will show with a colored container (surfaceVariant)
            JournalList(vm = vm, userId = uid, modifier = Modifier.fillMaxSize())
        }
    }
}