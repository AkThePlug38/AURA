@file:Suppress("DEPRECATION")

package com.Rajath.aura.player

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.C
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple singleton-style holder for a single ExoPlayer instance used across screens.
 * Create with init(context) from an Activity/Composable (LocalContext).
 */
object PlayerHolder {
    private var _player: ExoPlayer? = null
    val player: ExoPlayer?
        get() = _player

    // reactive state for isPlaying (useful in UI)
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    @MainThread
    fun init(context: Context) {
        if (_player != null) return
        _player = ExoPlayer.Builder(context.applicationContext).build().apply {
            val aa = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(aa, true)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = false
            // observe playWhenReady/state to update isPlaying
            addListener(object : com.google.android.exoplayer2.Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    _isPlaying.value = isPlayingNow
                }
            })
        }
    }

    @MainThread
    fun release() {
        try {
            _player?.release()
        } catch (_: Exception) {}
        _player = null
    }

    fun loadAndPlay(uri: Uri) {
        val p = _player ?: return
        p.stop()
        p.clearMediaItems()
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.playWhenReady = true
    }

    fun play() { _player?.playWhenReady = true }
    fun pause() { _player?.pause() }
    fun stop() {
        _player?.stop()
        _player?.clearMediaItems()
    }

    fun audioSessionId(): Int = _player?.audioSessionId ?: 0
}