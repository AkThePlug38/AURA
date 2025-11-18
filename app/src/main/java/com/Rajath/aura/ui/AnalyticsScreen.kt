@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.Rajath.aura.ui

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Rajath.aura.data.JournalEntry
import com.Rajath.aura.vm.JournalViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    uid: String,
    onBack: () -> Unit,
    vm: JournalViewModel = viewModel()
) {
    // subscribe once for this uid
    LaunchedEffect(uid) { vm.observeRecent(uid) }
    val items by vm.recentJournals.collectAsState()

    // sort chronologically oldest -> newest before computing analytics
    val sorted = remember(items) {
        items.filter { it.timestamp > 0L }.sortedBy { it.timestamp }
    }

    // compute analytics (using your computeMoodValue helper)
    val moodValues = remember(sorted) { sorted.map { computeMoodValue(it) } } // oldest->newest
    val sentimentCounts = remember(items) { computeSentimentCounts(items) }
    val topWords = remember(items) { computeTopWords(items, 10) }
    val streak = remember(items) { computeStreak(items) }

    // match background across screens (same as Journal/Home)
    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Scaffold(
            containerColor = Color.Transparent, // allow gradient to show through
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Analytics", color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // line chart card (trend)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Mood trend", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            LineChartCompose(entries = sorted, moodValues = moodValues)
                            Spacer(modifier = Modifier.height(8.dp))
                            val avg = if (moodValues.isNotEmpty()) (moodValues.average()).toFloat() else 0f
                            Text("Average mood: ${"%.2f".format(avg)}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Streak", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$streak days", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Entries", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${items.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sentiment distribution", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            PieChartCompose(counts = sentimentCounts)
                        }
                    }
                }

                // Top words card (modern chip grid) — keeps your Option 2 layout
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Top words",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                topWords.forEach { (word, count) ->
                                    AssistChip(
                                        onClick = { /* optional: filter by word or show examples */ },
                                        label = {
                                            Text(
                                                "$word • $count",
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Compose wrapper that renders a MPAndroidChart LineChart inside AndroidView **/
@Composable
private fun LineChartCompose(entries: List<JournalEntry>, moodValues: List<Float>) {

    if (moodValues.isEmpty() || entries.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No trend data yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Write a few journals to populate this chart.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        return
    }

    // ▼▼ ADD WRAPPER COLUMN WITH BOTTOM PADDING ▼▼
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)   // <— this adds the space you want
    ) {
        AndroidView(
            factory = { ctx ->
                LineChart(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        360
                    )
                    setDrawGridBackground(false)
                    setTouchEnabled(false)
                    setScaleEnabled(false)
                    axisRight.isEnabled = false
                    description = Description().apply { text = "" }
                    legend.isEnabled = false
                    setNoDataText("No chart data")
                }
            },
            update = { chartView ->
                val dataEntries =
                    moodValues.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }

                val dataSet = LineDataSet(dataEntries, "mood").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    setDrawCircles(false)
                    lineWidth = 2.5f
                    setDrawValues(false)
                    color = "#6673FF".toColorInt()
                    fillColor = "#66F0FF".toColorInt()
                    fillAlpha = 80
                }

                val data = LineData(dataSet)
                chartView.data = data

                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val labels = entries.map { sdf.format(Date(it.timestamp)) }

                val xAxis = chartView.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = AndroidColor.WHITE
                xAxis.granularity = 1f
                xAxis.labelRotationAngle = -30f
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.labelCount = labels.size.coerceAtMost(6)

                val left = chartView.axisLeft
                left.textColor = AndroidColor.WHITE
                left.axisMinimum = 0f
                left.axisMaximum = 1f
                left.textSize = 10f
                left.setDrawGridLines(true)
                left.gridColor = "#223447".toColorInt()

                chartView.setBackgroundColor(AndroidColor.TRANSPARENT)
                chartView.setViewPortOffsets(12f, 8f, 12f, 8f)
                chartView.invalidate()
            }
        )
    }
}

@Composable
fun PieChartCompose(
    counts: Map<String, Int>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,           // donut diameter
    strokeWidth: Dp = 34.dp      // donut thickness
) {
    val order = listOf("Positive", "Neutral", "Negative")
    val ordered = order.mapNotNull { k -> counts[k]?.let { k to it } } +
            counts.filterKeys { it !in order }.map { it.key to it.value }

    val total = ordered.sumOf { it.second }.takeIf { it > 0 } ?: 0

    if (total == 0) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No sentiment data yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Write a journal to populate the chart",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        return
    }

    val baseColors = listOf(
        Color(0xFF34D399), // positive
        Color(0xFFFBBF24), // neutral
        Color(0xFFFB7185)  // negative
    )
    val extraColors = listOf(
        Color(0xFF8B5CF6),
        Color(0xFF60A5FA),
        Color(0xFFF472B6),
        Color(0xFFF97316)
    )
    val colors = (baseColors + extraColors).take(ordered.size)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 30.dp),   // keep only top padding here
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- Chart block (explicitly separate) ---
        Box(
            modifier = Modifier
                .size(size)
                .padding(bottom = 6.dp), // small gap between canvas and legend
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val strokePx = with(density) { strokeWidth.toPx() }
            val sizePx = with(density) { size.toPx() }

            Canvas(modifier = Modifier.size(size)) {
                val diameter = sizePx
                val rect = Rect(0f, 0f, diameter, diameter)
                var startAngle = -90f

                ordered.forEachIndexed { index, pair ->
                    val sweep = (pair.second.toFloat() / total) * 360f
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += sweep
                }
            }
        }

        // --- Legend block (explicitly separate and padded) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp), // <-- ensures space BELOW the legend
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val legendItems = ordered.map { (label, count) -> label to count }
            val chunkSize =
                if (legendItems.size <= 3) legendItems.size else (legendItems.size + 1) / 2
            val rows = legendItems.chunked(chunkSize)

            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    row.forEach { (label, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        colors[ordered.indexOfFirst { it.first == label }],
                                        CircleShape
                                    )
                            )
                            Text(
                                "$label • $count",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun computeSentimentCounts(items: List<JournalEntry>): Map<String, Int> {
    val counts = mutableMapOf("Positive" to 0, "Neutral" to 0, "Negative" to 0)
    items.forEach {
        val category = resolveSentimentForCounting(it)
        counts[category] = counts.getOrDefault(category, 0) + 1
    }
    return counts
}

private val STOPWORDS = setOf(
    "the","and","a","i","to","of","is","it","in","that","was","for","on","with","as","have","this","but","be","are","my","you","so","not","they","at","or"
)

private fun computeTopWords(items: List<JournalEntry>, limit: Int = 10): List<Pair<String, Int>> {
    val freq = mutableMapOf<String, Int>()
    items.forEach { e ->
        val words = e.text
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length > 2 && it !in STOPWORDS }
        words.forEach { w -> freq[w] = freq.getOrDefault(w, 0) + 1 }
    }
    return freq.entries.sortedByDescending { it.value }.take(limit).map { it.key to it.value }
}

/** compute current streak in days */
private fun computeStreak(items: List<JournalEntry>): Int {
    if (items.isEmpty()) return 0
    // unique days with an entry
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = items.mapNotNull { e ->
        if (e.timestamp <= 0L) null else sdf.format(Date(e.timestamp))
    }.toSet().sortedDescending()
    if (dates.isEmpty()) return 0
    // count consecutive days from today backward
    var streak = 0
    val cal = Calendar.getInstance()
    val today = sdf.format(cal.time)
    var cursorDate = today
    while (true) {
        if (cursorDate in dates) {
            streak += 1
            cal.add(Calendar.DATE, -1)
            cursorDate = sdf.format(cal.time)
        } else break
    }
    return streak
}