package com.Rajath.aura.data

data class JournalEntry(
    val id: String = "",
    val text: String = "",
    val sentiment: String = "",
    val score: Double = 0.0,
    val timestamp: Long = 0L
)