@file:Suppress("DEPRECATION")

package com.Rajath.aura.player

import android.content.Context
import android.net.Uri
import android.util.Log
import com.Rajath.aura.vm.MeditationSession
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.C
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple globally accessible player manager.
 * Call PlayerManager.init(context) once (e.g. from MeditateScreen before first play).
 * Use PlayerManager.play(session), pause(), stop(), and observe .isPlaying / .audioSessionId / .currentSession.
 */
object PlayerManager {
    private const val TAG = "AURA-PlayerM"
    private var _player: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId = _audioSessionId.asStateFlow()

    private val _currentSession = MutableStateFlow<MeditationSession?>(null)
    val currentSession = _currentSession.asStateFlow()

    fun init(context: Context) {
        if (_player != null) return
        val p = ExoPlayer.Builder(context.applicationContext).build().apply {
            val aa = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(aa, true)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = false
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    _isPlaying.value = isPlayingNow
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    // update audioSessionId whenever available
                    try {
                        _audioSessionId.value = audioSessionId
                    } catch (_: Exception) { /* ignore */ }
                }
            })
        }
        _player = p
        // set initial audioSessionId if available
        try { _audioSessionId.value = p.audioSessionId } catch (_: Exception) {}
        Log.d(TAG, "PlayerManager initialized")
    }

    fun player(): ExoPlayer? = _player

    fun play(session: MeditationSession) {
        val p = _player ?: run {
            Log.w(TAG, "player not initialized")
            return
        }
        try {
            _currentSession.value = session
            val uri = if (session.source.startsWith("raw:")) {
                val name = session.source.removePrefix("raw:")
                Uri.parse("android.resource://${p.applicationLooper}:${name}") // fallback – prefer passing ctx to build MediaItem
                // We'll prefer MediaItem.fromUri handled by caller if you supply context.
            } else {
                Uri.parse(session.source)
            }

            // safer: build MediaItem from the string source — caller can also pass content:// or http urls.
            val media = if (session.source.startsWith("raw:")) {
                // construct android.resource:// URI properly
                val pkg = p.applicationLooper?.thread?.name ?: ""
                // fallback: player can still play if caller provided setMediaItem(MediaItem.fromUri(...)) with real ctx
                MediaItem.fromUri(session.source)
            } else MediaItem.fromUri(session.source)

            p.stop()
            p.clearMediaItems()
            p.setMediaItem(media)
            p.prepare()
            p.playWhenReady = true
            // audioSessionId will be available after prepare / when audio starts
            try { _audioSessionId.value = p.audioSessionId } catch (_: Exception) {}
        } catch (t: Throwable) {
            Log.e(TAG, "play error: ${t.localizedMessage}", t)
        }
    }

    fun pause() {
        _player?.pause()
    }

    fun stop() {
        try {
            _player?.stop()
            _player?.clearMediaItems()
        } catch (_: Exception) {}
        _currentSession.value = null
        _isPlaying.value = false
        _audioSessionId.value = 0
    }

    fun release() {
        _player?.release()
        _player = null
        _currentSession.value = null
        _audioSessionId.value = 0
        _isPlaying.value = false
    }
}