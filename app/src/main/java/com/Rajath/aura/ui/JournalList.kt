package com.Rajath.aura.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Rajath.aura.data.JournalEntry
import com.Rajath.aura.vm.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalList(
    vm: JournalViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    // start observing when this composable is first composed
    LaunchedEffect(userId) {
        vm.observeRecent(userId)
    }

    val items by vm.recentJournals.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Recent entries", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = { vm.observeRecent(userId) }) {
                Text("Refresh", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            Text(
                text = "No journals yet. Your recent entries will show here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(12.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(items) { entry ->
                JournalListItem(entry = entry, onDelete = { vm.deleteJournal(userId, it) })
            }
        }
    }
}

@Composable
private fun JournalListItem(entry: JournalEntry, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.text.take(260).trimEnd(),
                    maxLines = 4,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    val sdf = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
                    val dateStr = if (entry.timestamp > 0L) sdf.format(Date(entry.timestamp)) else "—"
                    Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = entry.sentiment, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                }
            }

            IconButton(onClick = { onDelete(entry.id) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}