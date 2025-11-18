@file:Suppress("DEPRECATION")

package com.Rajath.aura.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri

/**
 * MeditateViewModel containing:
 * - list of sessions
 * - current session
 * - ExoPlayer instance
 * - play/pause/stop controls
 * - isPlaying StateFlow
 */
@Suppress("DEPRECATION")
class MeditateViewModel(application: Application) : AndroidViewModel(application) {

    // --- sessions ----------------------------------------------------------

    private val _sessions = MutableStateFlow(
        listOf(
            MeditationSession(
                id = "breathing",
                title = "Breathing Focus",
                subtitle = "Calm your breath — looped ambient guidance",
                source = "raw:meditation_breathing"
            ),
            MeditationSession(
                id = "bodyscan",
                title = "Body Scan",
                subtitle = "Full body relaxation — slow guided scan",
                source = "raw:meditation_body_scan"
            ),
            MeditationSession(
                id = "sleep_short",
                title = "Sleep Calm",
                subtitle = "Short winddown session for sleep",
                source = "raw:meditation_sleep_short"
            )
        )
    )
    val sessions = _sessions.asStateFlow()

    // --- currently selected / playing session ------------------------------

    private val _currentSession = MutableStateFlow<MeditationSession?>(null)
    val currentSession: StateFlow<MeditationSession?> = _currentSession.asStateFlow()

    // --- playing state -----------------------------------------------------

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // --- ExoPlayer ---------------------------------------------------------

    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        val attrs = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        setAudioAttributes(attrs, true)
        repeatMode = ExoPlayer.REPEAT_MODE_ONE
        playWhenReady = false
    }

    init {
        // Update isPlaying as player updates
        player.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                _isPlaying.value = isPlayingNow
            }
        })
    }

    // --- playback control --------------------------------------------------

    fun play(session: MeditationSession) {
        viewModelScope.launch {
            try {
                _currentSession.value = session
                player.stop()
                player.clearMediaItems()

                val uri = toUri(session.source)
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.playWhenReady = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _currentSession.value = null
        _isPlaying.value = false
    }

    // helper
    private fun toUri(source: String): Uri {
        return if (source.startsWith("raw:")) {
            val name = source.removePrefix("raw:")
            "android.resource://${getApplication<Application>().packageName}/raw/$name".toUri()
        } else source.toUri()
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}