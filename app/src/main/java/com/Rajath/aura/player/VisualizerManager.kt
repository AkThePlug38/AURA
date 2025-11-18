package com.Rajath.aura.player

import android.media.audiofx.Visualizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.hypot

/**
 * Lightweight Visualizer wrapper. Start with start(sessionId) and stop() when done.
 * Emits amplitude in 0..1 range via amplitudeFlow.
 */
class VisualizerManager {
    private var visualizer: Visualizer? = null
    private var job: Job? = null
    private val _amplitude = MutableStateFlow(0f)
    val amplitudeFlow: StateFlow<Float> = _amplitude

    fun start(sessionId: Int) {
        stop()
        if (sessionId <= 0) return
        try {
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // max
                enabled = true
            }
            // poll periodically and compute RMS amplitude from waveform
            job = CoroutineScope(Dispatchers.Default).launch {
                val buf = ByteArray(visualizer!!.captureSize)
                while (isActive) {
                    try {
                        val read = visualizer?.getWaveForm(buf)
                        if (read == Visualizer.SUCCESS) {
                            // compute RMS-like amplitude
                            var acc = 0.0
                            for (b in buf) {
                                val v = (b.toInt() and 0xff) - 128
                                acc += (v * v).toDouble()
                            }
                            val mean = if (buf.isNotEmpty()) acc / buf.size else 0.0
                            val rms = kotlin.math.sqrt(mean)
                            // normalize (rough heuristic) -> 0..1
                            val normalized = (rms / 128.0).coerceIn(0.0, 1.0).toFloat()
                            _amplitude.value = normalized
                        } else {
                            // fallback small pulsing when waveform not available
                            _amplitude.value = 0f
                        }
                    } catch (_: Exception) {
                        _amplitude.value = 0f
                    }
                    delay(80L)
                }
            }
        } catch (t: Throwable) {
            visualizer = null
            job?.cancel()
            job = null
        }
    }

    fun stop() {
        try {
            job?.cancel()
            job = null
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
        _amplitude.value = 0f
    }
}