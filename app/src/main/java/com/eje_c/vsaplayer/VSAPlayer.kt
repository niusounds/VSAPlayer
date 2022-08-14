package com.eje_c.vsaplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.ext.gvr.CustomGvrAudioProcessor
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class VSAPlayer(context: Context) : Player.Listener {

    private val audioProcessor = CustomGvrAudioProcessor()
    private val dataSourceFactory = DefaultDataSourceFactory(context, context.packageName)
    private val exoPlayer: SimpleExoPlayer

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
                enableAudioTrackPlaybackParams: Boolean,
                enableOffload: Boolean
            ): AudioSink? {
                return DefaultAudioSink(
                    AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES,
                    DefaultAudioSink.DefaultAudioProcessorChain(audioProcessor).audioProcessors
                )
            }
        }

        exoPlayer = SimpleExoPlayer.Builder(context, renderersFactory).build()
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
