package com.Rajath.aura.ui

import com.Rajath.aura.data.JournalEntry
import java.util.*

/**
 * Normalize/standardize a string label into one of "Positive", "Neutral", "Negative" (case-insensitive).
 * Handles HF label names like LABEL_2, POSITIVE, neutral, etc.
 */
fun normalizeLabel(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val s = raw.trim().lowercase(Locale.getDefault())
    return when {
        s.contains("pos") || s == "label_2" || s == "label2" -> "Positive"
        s.contains("neu") || s == "label_1" || s == "label1" -> "Neutral"
        s.contains("neg") || s == "label_0" || s == "label0" -> "Negative"
        else -> null
    }
}

/**
 * Map label -> base numeric mood (0..1)
 * Negative ~ 0.12..0.28, Neutral ~ 0.45..0.55, Positive ~ 0.72..0.88 (centred at 0.2/0.5/0.8)
 */
private fun labelBaseValue(label: String): Float = when (label) {
    "Positive" -> 0.8f
    "Neutral" -> 0.5f
    "Negative" -> 0.2f
    else -> 0.5f
}

/**
 * Compute a final mood value [0f..1f] from a JournalEntry.
 * Strategy:
 *  - If label exists, use label base (dominant) blended with confidence
 *  - If label is missing, infer label from score thresholds
 */
fun computeMoodValue(entry: JournalEntry): Float {
    // clamp score into 0..1
    val score = entry.score.coerceIn(0.0, 1.0).toFloat()

    val normalized = normalizeLabel(entry.sentiment)
    return if (normalized != null) {
        // Blend: label dominates (75%), confidence fine-tunes (25%)
        val base = labelBaseValue(normalized)
        (base * 0.75f + score * 0.25f).coerceIn(0f, 1f)
    } else {
        // fallback purely from confidence thresholds (still returns full-range)
        when {
            score >= 0.66f -> (0.8f * 0.75f + score * 0.25f).coerceIn(0f, 1f) // positive-ish
            score >= 0.33f -> (0.5f * 0.75f + score * 0.25f).coerceIn(0f, 1f) // neutral-ish
            else -> (0.2f * 0.75f + score * 0.25f).coerceIn(0f, 1f) // negative-ish
        }
    }
}

/**
 * Resolve the final canonical sentiment string for counts/UI, preferring label when available,
 * but falling back to score thresholds.
 */
fun resolveSentimentForCounting(entry: JournalEntry): String {
    normalizeLabel(entry.sentiment)?.let { return it }
    val s = entry.score.toFloat().coerceIn(0f, 1f)
    return when {
        s >= 0.66f -> "Positive"
        s >= 0.33f -> "Neutral"
        else -> "Negative"
    }
}