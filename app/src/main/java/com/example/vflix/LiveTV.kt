package com.example.vflix

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import java.nio.charset.StandardCharsets

fun urlSafeb64Encode(i: String) = android.util.Base64.encodeToString(i.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)

@RequiresApi(Build.VERSION_CODES.Q)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun LiveTV() {
    val context = LocalContext.current
    val isF = remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    LaunchedEffect(Unit) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                isF.value = true
                context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }

            else -> {
                isF.value = false
                context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        }
    }

    val trackSelector = DefaultTrackSelector(context)
    var mpdUrl =
        "https://bpprod6linear.akamaized.net/bpk-tv/irdeto_com_Channel_464/output/manifest.mpd?headers=eyJob3N0IjoiYnBwcm9kNmxpbmVhci5ha2FtYWl6ZWQubmV0In0%3D"
    val k = encodeHexToURLSafeBase64("bc0aaa542e06b1816fea83b30d26be2c")
    val kid = urlSafeb64Encode("afa116dde11a5b0882404230854616be")
    println("XKEY: ${k}")
    val clearkeyJSON =
        "{\"keys\":[{\"kty\":\"oct\",\"k\":\"vAqqVC4GsYFv6oOzDSa-LA\",\"kid\":\"r6EW3eEaWwiCQEIwhUYWvg\"}],\"type\":\"temporary\"}"
    val clearkeyDrmSessionManager: DrmSessionManager = DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
        //.setKeyRequestParameters(HashMap(mapOf("Host" to "bpprod6linear.akamaized.net")))
        .build(
            LocalMediaDrmCallback(clearkeyJSON.toByteArray(StandardCharsets.UTF_8))
        )

    val player: ExoPlayer = remember {
        ExoPlayer.Builder(context).setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDrmSessionManagerProvider { clearkeyDrmSessionManager }
            )
            .build()
            .apply {
                prepare()
            }
    }

    player.addMediaItem(
        MediaItem.Builder()
            .setUri(mpdUrl)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setDrmConfiguration(DrmConfiguration.Builder(C.WIDEVINE_UUID).build())
            .setSubtitles(
                listOf(
                    MediaItem.Subtitle(
                        Uri.parse("https://bpprod6linear.akamaized.net/bpk-tv/irdeto_com_Channel_464/output/manifest.mpd?headers=eyJob3N0IjoiYnBwcm9kNmxpbmVhci5ha2FtYWl6ZWQubmV0In0%3D"),
                        MimeTypes.APPLICATION_MPD,
                        "en",
                        C.SELECTION_FLAG_DEFAULT
                    )
                )
            )
            .build()
    )

    

    player.prepare()

    Column(
        modifier = if (isF.value) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        } else {
            Modifier
                .height(260.dp)
                .fillMaxWidth()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    this.player = player
                    player.playWhenReady = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setControllerOnFullScreenModeChangedListener { isFullScreen ->
                        with(context) {
                            if (isFullScreen) {
                                isF.value = true
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                            } else {
                                isF.value = false
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 0.dp, 0.dp)
                .background(Color.Black)
        )
    }
}

fun encodeHexToURLSafeBase64(hex: String): String {
    val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    val encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
    return encoded.replace("=+$".toRegex(), "")
}

