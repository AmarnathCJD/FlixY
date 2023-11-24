package com.example.vflix

import android.os.Build
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

class CustomOnScaleGestureListener(
    private val player: PlayerView
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private var scaleFactor = 0f

    override fun onScale(
        detector: ScaleGestureDetector
    ): Boolean {
        scaleFactor = detector.scaleFactor
        return true
    }

    override fun onScaleBegin(
        detector: ScaleGestureDetector
    ): Boolean {
        return true
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onScaleEnd(detector: ScaleGestureDetector) {
        println("ScaleFactor: $scaleFactor")
        if (scaleFactor > 1) {
            player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun pley() {
    val ctx = LocalContext.current
    //video_view = PlayerView(ctx)

    val trackSelector = remember {
        DefaultTrackSelector(ctx).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredAudioLanguage("en")
                    .setPreferredTextLanguage("en")
                    .setForceHighestSupportedBitrate(true)
            )
        }
    }
    val player = remember {
        ExoPlayer.Builder(ctx).setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(ctx)
            )
            .build()
            .apply {
                prepare()
            }
    }

    video_view?.player = player
    video_view?.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)

    val mediaItem = remember {
        MediaItem.Builder()
            .setUri("https://ctrl.laotv.la/live/StarMovie/index.m3u8")
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
    }

    player.setMediaItem(mediaItem)

    player.prepare()

    println("Availabe Tracks: ${video_view!!.subtitleView.toString()}")

    /*AndroidView(
        factory = {
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                player?.playWhenReady = true
                video_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            //.padding(16.dp)
    )*/

    AndroidView(
        factory = { video_view!! },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
        //.padding(16.dp)
    )

    video_view!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

    ctx.setScreenOrientation(0)


}

@Composable
fun ZoomButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Zoom In"
        )
    }
}


@Composable
fun ZoomablePlayerView() {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale *= zoom
                }
            }
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    ) {
        AndroidView(
            factory = { video_view!! },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
            //.padding(16.dp)
        )
    }
}