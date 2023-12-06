package com.eje_c.vsaplayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ext.gvr.CustomGvrAudioProcessor

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VSAPlayer(context: Context) : Player.Listener {

    private val audioProcessor = CustomGvrAudioProcessor()
    private val dataSourceFactory = DefaultDataSource.Factory(context)
    private val exoPlayer: ExoPlayer

    /**
     * Callback for playback completion.
     */
    var onPlayEnd: (() -> Unit)? = null

    val isPlaying: Boolean
        get() = exoPlayer.playWhenReady

    /**
     * Playback position in milliseconds.
     */
    var currentTimeInMillis: Long
        get() = exoPlayer.currentPosition
        set(value) = exoPlayer.seekTo(value)

    /**
     * Duration of the current content in milliseconds.
     */
    val duration: Long
        get() = exoPlayer.duration

    init {

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(audioProcessor))
                    .build()
            }
        }

        exoPlayer = ExoPlayer.Builder(context, renderersFactory).build()
        exoPlayer.addListener(this)
    }

    fun prepare(uri: Uri) {
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    fun play() {
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun updateOrientation(x: Float, y: Float, z: Float, w: Float) {
        audioProcessor.updateOrientation(w, x, y, z)
    }

    fun release() {
        exoPlayer.release()
        onPlayEnd = null
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_ENDED -> {
                onPlayEnd?.invoke()
            }
        }
    }
}
