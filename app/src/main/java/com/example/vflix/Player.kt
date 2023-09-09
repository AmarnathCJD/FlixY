package com.example.vflix

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    MyContent()
}

// Creating a composable function to create
// two Images and a spacer between them
// Calling this function as content in the above function
@Composable
fun MyContent(){

    Column(Modifier.fillMaxSize()) {
        val context = LocalContext.current

        val exoPlayer = remember {
            ExoPlayer.Builder(context)
                .build()
        }



        val playerView = remember {
            StyledPlayerView(context).apply {
                player = exoPlayer
                useController = true
                //layoutParams = ViewGroup.LayoutParams(1280, 800)
                setControllerOnFullScreenModeChangedListener { isFullScreen ->
                    with(context) {
                        if (isFullScreen) {
                            setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                        } else {
                            setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        }
                    }
                }
                setShowSubtitleButton(true)
                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_ALWAYS)

                exoPlayer.setMediaItem(
                    MediaItem.Builder()
                        .setUri("https://eerht.artdesigncdn.net/_v10/81b16d8ff2956d3be046729121df2350c2fc12653e68857e2d5c18572afdca2b0ee07bcafb1eb90d55230f164691180ecf90e2e25e8af40e1b53438f22e06626742e1d806f5c283f8206515823e9e444ba9adeeff8961b670e99d950073a937e0cc77d859e8124581bb9fc369f43d629adf36b1fbb5e00ff6d3542793086c7acd4908f366a6cc62ccf2f98e9ba250b92/playlist.m3u8")
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                )
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }


        AndroidView(
            factory = { playerView }
        )

    }
}
fun Context.findActivity(): Activity? = when (this) {
    is Activity       -> this
    is ContextWrapper -> baseContext.findActivity()
    else              -> null
}

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this.findActivity() ?: return
    activity.requestedOrientation = orientation
    if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        hideSystemUi()
    } else {
        showSystemUi()
    }
}

fun Context.hideSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Context.showSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}