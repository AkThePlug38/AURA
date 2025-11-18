package com.Rajath.aura.ui

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.Rajath.aura.data.JournalEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.core.graphics.toColorInt

@Composable
fun SparklineChart(entries: List<JournalEntry>, modifier: Modifier = Modifier) {
    // sort chronologically: oldest -> newest
    val sorted = entries
        .filter { it.timestamp > 0L }      // optional: ignore invalid timestamps
        .sortedBy { it.timestamp }         // oldest first
    val scores = sorted.map { computeMoodValue(it) } // use your computeMoodValue helper

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
                setDrawGridBackground(false)
                setTouchEnabled(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                axisRight.isEnabled = false
                axisLeft.isEnabled = false
                xAxis.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = false
                setViewPortOffsets(0f, 0f, 0f, 0f) // make it edge-to-edge inside the card
            }
        },
        update = { chart ->
            if (scores.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val dataEntries = scores.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }
            val ds = LineDataSet(dataEntries, "").apply {
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1.6f
                color = "#66F0FF".toColorInt()
                setDrawFilled(true)
                fillColor = "#6673FF".toColorInt()
                fillAlpha = 60
                mode = LineDataSet.Mode.LINEAR
            }

            val lineData = LineData(listOf(ds))
            chart.data = lineData

            // notify MPAndroidChart about the data change (important!)
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()

            // optional viewport tweaks so the line uses the full width
            chart.setVisibleXRangeMaximum( (dataEntries.size - 1).coerceAtLeast(1).toFloat() )
            chart.moveViewToX((dataEntries.size - 1).toFloat())

            chart.invalidate()
        },
        modifier = modifier
    )
}