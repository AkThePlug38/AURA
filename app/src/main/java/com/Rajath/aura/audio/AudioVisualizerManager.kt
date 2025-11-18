package com.Rajath.aura.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Simple manager that attaches Android Visualizer to an audio session id and exposes a 0..1 amplitude.
 *
 * Usage:
 *   val avm = AudioVisualizerManager()
 *   avm.start(audioSessionId)
 *   avm.amplitude.collect { ... }    // or avm.amplitude.collectAsState() in Compose
 *   avm.stop()
 *   avm.release()
 *
 * Notes:
 * - Works only on real devices (emulator often doesn't support Visualizer).
 * - Some OEMs / Bluetooth routes may block capture; amplitude may be small -> tweak `sensitivityDivisor`.
 */
class AudioVisualizerManager {

    companion object {
        private const val TAG = "AURA-AVM"
    }

    private var visualizer: Visualizer? = null

    // amplitude 0..1 exposed as StateFlow
    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    @Synchronized
    fun start(audioSessionId: Int) {
        stop() // ensure single instance

        try {
            Log.d(TAG, "start() audioSessionId=$audioSessionId")

            if (audioSessionId <= 0) {
                Log.w(TAG, "audioSessionId invalid ($audioSessionId) — aborting start()")
                scope.launch { _amplitude.value = 0f }
                return
            }

            val v = Visualizer(audioSessionId)

            // choose capture size safely (use max if available)
            val sizeRange = try {
                Visualizer.getCaptureSizeRange()
            } catch (t: Throwable) {
                Log.w(TAG, "getCaptureSizeRange() failed: ${t.localizedMessage}")
                intArrayOf(256, 1024)
            }
            val captureSize = sizeRange.maxOrNull() ?: 1024
            try { v.captureSize = captureSize } catch (_: Throwable) { /* ignore */ }

            val rate = try {
                (Visualizer.getMaxCaptureRate() / 2).coerceAtLeast(4000)
            } catch (_: Throwable) {
                8000
            }

            v.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                    if (waveform == null || waveform.isEmpty()) {
                        scope.launch { _amplitude.value = 0f }
                        return
                    }

                    // RMS
                    var sum = 0.0
                    for (b in waveform) {
                        val vInt = (b.toInt() and 0xFF) - 128
                        sum += (vInt * vInt).toDouble()
                    }
                    val mean = sum / waveform.size.toDouble()
                    val rms = sqrt(mean).toFloat()

                    // adjust this divisor if amplitude is too small/large on device
                    val sensitivityDivisor = 120f
                    val normalized = (rms / sensitivityDivisor).coerceIn(0f, 1f)

                    scope.launch { _amplitude.value = normalized }
                }

                override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    // optional — not used now
                }
            }, rate, true, false)

            v.enabled = true
            visualizer = v
            Log.d(TAG, "Visualizer enabled (captureSize=$captureSize, rate=$rate)")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start Visualizer: ${t.localizedMessage}", t)
            scope.launch { _amplitude.value = 0f }
        }
    }

    @Synchronized
    fun stop() {
        try {
            visualizer?.let {
                Log.d(TAG, "stop() visualizer disabling")
                it.release()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error releasing visualizer: ${t.localizedMessage}", t)
        } finally {
            visualizer = null
            scope.launch { _amplitude.value = 0f }
        }
    }

    fun release() {
        stop()
    }
}
