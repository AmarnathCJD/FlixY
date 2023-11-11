package com.example.vflix

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.parser.DRM
import com.example.vflix.parser.GetDRM
import com.example.vflix.parser.MainChannels
import com.example.vflix.ui.theme.sans_bold
import java.nio.charset.StandardCharsets

val currentDRM = mutableStateOf(DRM("", "", "", "", "", ""))
val IsLiveTVHome = mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun LiveTV(nav: NavHostController) {
    if (IsLiveTVHome.value) {
        genLiveTVHome()
    } else {
        val context = LocalContext.current
        val isF = remember { mutableStateOf(false) }
        val configuration = LocalConfiguration.current
        LaunchedEffect(Unit) {
            GetDRM(clickedID, currentDRM)
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

        if (currentDRM.value.mpd == "" || clickedID != currentDRM.value.id) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(Color.Black),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preparing Stream...",
                    fontFamily = sans_bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
            }
        } else {
            val k = encodeHexToURLSafeBase64(currentDRM.value.key)
            val kid = encodeHexToURLSafeBase64(currentDRM.value.keyId)
            val clearkeyJSON =
                "{\"keys\":[{\"kty\":\"oct\",\"k\":\"$k\",\"kid\":\"$kid\"}],\"type\":\"temporary\"}"
            val clearkeyDrmSessionManager: DrmSessionManager = DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
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

            BackHandler {
                if (isF.value) {
                    isF.value = false
                    context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                } else {
                    player.stop()
                    player.release()
                    if (prevPageHistory.size == 1) {
                        nav.navigate("homePage")
                    } else {
                        val prevPage = prevPageHistory.last()
                        clickedID = prevPage.prevID
                        clickedName = prevPage.prevName
                        prevPageHistory.removeLast()
                        mediaType = prevPage.prevMediaType
                        searchValue.value = prevPage.prevSearch
                        nav.navigate(prevPage.prevPage)
                    }
                }
            }

            ComposableLifecycle { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        player.play()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        player.pause()
                    }

                    else -> {}
                }
            }

            Column(
                modifier = if (isF.value) {
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                } else {
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .verticalScroll(rememberScrollState())
                },
            ) {

                if (!isF.value) {
                    TopAppBar(
                        title = {
                            Row {
                                Image(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(64.dp)
                                        .padding(horizontal = 12.dp, vertical = 15.dp)
                                        .clickable {
                                            player.stop()
                                            killThreads = true
                                            if (prevPageHistory.size == 1) {
                                                nav.navigate("homePage")
                                            } else {
                                                val prevPage = prevPageHistory.last()
                                                clickedID = prevPage.prevID
                                                clickedName = prevPage.prevName
                                                prevPageHistory.removeLast()
                                                mediaType = prevPage.prevMediaType
                                                searchValue.value = prevPage.prevSearch
                                                nav.navigate(prevPage.prevPage)
                                            }
                                        },
                                    contentScale = ContentScale.Crop,
                                    colorFilter =
                                    ColorFilter.lighting(
                                        add = Color(0xFFFFFFFF),
                                        multiply = Color.White
                                    )
                                )
                                Row {
                                    Text(
                                        text = "Streaming: ",
                                        fontFamily = sans_bold,
                                        color = Color.White, fontSize = 14.sp,
                                        modifier = Modifier
                                            .height(64.dp)
                                            .padding(vertical = 23.dp)
                                            .padding(
                                                start = 12.dp,
                                                end = 2.dp
                                            )
                                    )
                                    Text(
                                        text = "$clickedName ",
                                        fontFamily = sans_bold,
                                        color = Color.White, fontSize = 14.sp,
                                        modifier = Modifier
                                            .height(64.dp)
                                            .padding(vertical = 23.dp)
                                            .padding(
                                                start = 2.dp,
                                                end = 12.dp
                                            )
                                            .basicMarquee()
                                    )
                                }
                            }
                        },
                        colors =
                        TopAppBarDefaults.smallTopAppBarColors(
                            titleContentColor = Color.Transparent,
                            containerColor = Color.Transparent,
                        ),
                    )
                }

                player.addMediaItem(
                    MediaItem.Builder()
                        .setUri(currentDRM.value.mpd + "?headers=eyJob3N0IjoiYnBwcm9kM2xpbmVhci5ha2FtYWl6ZWQubmV0In0%3D")
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .setDrmConfiguration(DrmConfiguration.Builder(C.WIDEVINE_UUID).build())
                        .build()
                )

                player.prepare()

                Column(
                    modifier = if (isF.value) {
                        Modifier
                            .fillMaxSize()
                            .fillMaxHeight()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(260.dp)
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
                Column {
                    channelAbout()
                }
            }
        }
    }
}

fun encodeHexToURLSafeBase64(hex: String): String {
    val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    val encoded = android.util.Base64.encodeToString(
        bytes,
        android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
    )
    return encoded.replace("=+$".toRegex(), "")
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun channelAbout() {
    Column(
        modifier = Modifier
            .background(Color.Black)
            .background(
                Color(0xFF000000)
            )
            .height(160.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        val showShimmer = remember { mutableStateOf(true) }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 0.dp)
                .background(
                    Color.Black
                )
        ) {
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(mediaType)
                    .crossfade(true)
                    .build(),
                contentDescription = "Channel Thumb",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        shimmerBrush(
                            targetValue = 1300f,
                            showShimmer = showShimmer.value
                        )
                    )
                    .background(
                        Color(0xFF1F1F1F)
                    )
                    .height(130.dp)
                    .width(130.dp),
                onSuccess = {
                    showShimmer.value = false
                }
            )
            Spacer(modifier = Modifier.width(7.dp))
            Column {
                Text(
                    text = clickedName,
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "[LIVE]",
                    fontFamily = sans_bold,
                    color = Color.Blue,
                    fontSize = 14.sp
                )

                if (currentDRM.value.curr != "") {
                    Row {
                        Text(
                            text = "Now Playing: ",
                            fontFamily = sans_bold,
                            fontSize = 14.sp,
                            color = Color.White,
                        )

                        Text(
                            text = currentDRM.value.curr,
                            fontFamily = sans_bold,
                            fontSize = 14.sp,
                            color = Color(0xFFC7D62E),
                        )
                    }
                }
            }
        }
    }
}

// TODO: Fetch and display similar channels list

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun genLiveTVHome() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .background(Color.Black)
        ) {
            Row {
                Column {
                    Text(
                        text = "Live TV",
                        fontFamily = sans_bold,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(20.dp, 20.dp, 0.dp, 0.dp)
                    )
                }
            }
        }

        val channels = MainChannels
        val channels_grouped = channels.chunked(3)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(channels_grouped.size) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    channels_grouped[index].forEach { channel ->
                        Column {
                            val showShimmer = remember { mutableStateOf(true) }
                            AsyncImage(
                                model =
                                ImageRequest.Builder(LocalContext.current)
                                    .data(channel.image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Channel Thumb",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    )
                                    .background(
                                        Color(0xFF1F1F1F)
                                    )
                                    .height(100.dp)
                                    .width(100.dp),
                                onSuccess = {
                                    showShimmer.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// TODO: Add search functionality
// TODO: Add Filter functionality