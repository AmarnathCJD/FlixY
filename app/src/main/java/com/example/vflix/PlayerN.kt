package com.example.vflix

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.api.FetchTrailer
import com.example.vflix.api.GatherEmbedURL
import com.example.vflix.api.GatherEpisodes
import com.example.vflix.api.GatherNTInSync
import com.example.vflix.api.M3U8QualitiesSync
import com.example.vflix.api.NSubtitle
import com.example.vflix.api.NT
import com.example.vflix.api.NTQuality
import com.example.vflix.ui.theme.sans_bold

var activeTitle = mutableStateOf<NT?>(null)
var activeSE = mutableStateOf(Pair(0, 0))
var ActiveM3U8 = mutableStateOf("")
val currentSeek = mutableStateOf(0L)
val showLoading = mutableStateOf(true)
val showNoSource = mutableStateOf(false)
val isPlaying = mutableStateOf(false)
val isF = mutableStateOf(false)
val currentQualityUrl = mutableStateOf("")
val availQualities = mutableStateOf(listOf<NTQuality>())
val Subs = mutableStateOf(listOf<NSubtitle>())


@RequiresApi(Build.VERSION_CODES.Q)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerN(nav: NavHostController) {
    val context = LocalContext.current
    context.filesDir

    val trackSelector = DefaultTrackSelector(context)

    val player = remember {
        ExoPlayer.Builder(context).setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
            )
            .build()
            .apply {
                prepare()
            }
    }
    var shouldUpdateM3U8 = false
    val configuration = LocalConfiguration.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.Black)

    ) {
        LaunchedEffect(Unit) {
            if (activeTitle.value?.href != clickedID) {
                GatherNTInSync(
                    clickedID,
                    activeTitle,
                    activeSE,
                    ActiveM3U8,
                    Subs,
                    showLoading,
                    showNoSource
                )
                shouldUpdateM3U8 = true
            }
        }

        if (activeTitle.value == null) {
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
            LaunchedEffect(Unit) {
                if (activeTitle.value?.href != clickedID || shouldUpdateM3U8) {
                    GatherEmbedURL(
                        activeTitle,
                        activeSE,
                        ActiveM3U8,
                        Subs,
                        showLoading,
                        showNoSource
                    )
                    shouldUpdateM3U8 = false
                }
            }

            if (ActiveM3U8.value != "") {
                if (ActiveM3U8.value.endsWith("playlist.m3u8")) {
                    Thread { M3U8QualitiesSync(ActiveM3U8.value, availQualities) }.start()
                }
            }

            if (ActiveM3U8.value == "" || activeTitle.value?.href != clickedID || activeTitle.value!!.genJS || showNoSource.value || showLoading.value) {
                Column(
                    modifier = Modifier
                        .height(260.dp)
                        .fillMaxWidth()
                        .padding(
                            top = 15.dp,
                        )
                        .border(
                            width = 0.dp,
                            color = Color.Black
                        )
                        .background(Color.Black),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color.Red,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                    )
                }
            } else {
                if (showLoading.value) {
                    showLoading.value = false
                }
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

                BackHandler {
                    if (isF.value) {
                        isF.value = false
                        context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    } else {
                        player.stop()
                        isPlaying.value = false
                        if (prevPageHistory.size == 1) {
                            nav.navigate("homePage")
                        } else {
                            val lastPage = prevPageHistory.last().prevPage
                            nav.navigate(lastPage)
                        }
                    }
                }

                ComposableLifecycle { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            player.play()
                            isPlaying.value = true
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            player.pause()
                            isPlaying.value = false
                        }

                        else -> {}
                    }
                }

                var mimeType = MimeTypes.APPLICATION_M3U8
                val subs = mutableListOf<SubtitleConfiguration>()

                if (ActiveM3U8.value.contains("googlevideo.com")) {
                    mimeType = MimeTypes.APPLICATION_MP4
                } else {
                    for (sub in Subs.value) {
                        val subUri = Uri.parse(sub.uri)
                        val subMimeType = MimeTypes.TEXT_VTT
                        val subLang = sub.lang
                        var selectionFlags = C.SELECTION_FLAG_AUTOSELECT
                        if (sub.default) {
                            selectionFlags = C.SELECTION_FLAG_DEFAULT
                        }
                        val subConfig = SubtitleConfiguration.Builder(subUri)
                            .setLabel(subLang)
                            .setMimeType(subMimeType)
                            .setLanguage(subLang)
                            .setSelectionFlags(selectionFlags)
                            .build()

                        subs.add(subConfig)
                    }
                }


                val media = MediaItem.Builder()
                    .setUri(ActiveM3U8.value)
                    .setMimeType(mimeType)
                    .setSubtitleConfigurations(subs)
                    .build()

                if ((player.currentMediaItem?.playbackProperties?.uri.toString() == ActiveM3U8.value)) {
                    //skip
                } else {
                    player.setMediaItem(media)
                }

                val runnableThreadToCapturePlaybackPositions = Runnable {
                    while (true) {
                        Thread.sleep(1000)
                    }
                }

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == ExoPlayer.STATE_READY) {
                            showLoading.value = false
                            if (currentSeek.value != 0L) {
                                player.seekTo(currentSeek.value)
                                currentSeek.value = 0
                            }
                        } else if (state == ExoPlayer.STATE_ENDED) {
                            player.seekTo(0)
                            player.play()
                        } else if (state == ExoPlayer.STATE_IDLE) {
                            player.prepare()
                        } else if (state == ExoPlayer.STATE_BUFFERING) {
                            showLoading.value = true
                        }
                    }
                }, )

                //Thread(runnableThreadToCapturePlaybackPositions).start()

                if (currentSeek.value != 0L) {
                    player.seekTo(currentSeek.value)
                }
                player.prepare()
                // player.stop()

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
                    AndroidView(factory = { context ->
                        LayoutInflater.from(context).inflate(R.layout.activity_player, null)
                    }, update = {
                        video_view = it.findViewById(R.id.playerView)
                        video_view?.player = player
                        video_view?.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        val zoomButton = it.findViewById<ImageView>(R.id.zoomButton)
                        val rotateButton = it.findViewById<ImageView>(R.id.rotateButton)
                        val playerView = video_view!!
                        val qualityButton = it.findViewById<ImageView>(R.id.qualityButton)
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        playerView.setControllerVisibilityListener(
                            PlayerControlView.VisibilityListener { visibility ->
                                zoomButton.visibility =
                                    if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                                rotateButton.visibility =
                                    if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                                qualityButton.visibility =
                                    if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                            }
                        )
                        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)


                        playerView.showController()
                    },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                            .background(Color.Black)
                    )
                }
            }

            if (!isF.value && activeTitle.value!!.href == clickedID) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),

                    ) {
                    Divider(
                        color = Color.DarkGray,
                        modifier = Modifier
                            .height(1.dp)
                    )
                    QualityPopup { quality ->
                        currentQualityUrl.value = quality
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    PlayTrailerButton(player)
                    Spacer(modifier = Modifier.height(15.dp))
                    TitleMedata()
                    SetupSeasonAndEpisodeSelector(player)
                    Divider(
                        color = Color.DarkGray,
                        modifier = Modifier
                            .height(1.dp)
                    )

                    SetupSimilar(nav)
                }
            } else if (isF.value) {
                QualityPopup { quality ->
                    currentQualityUrl.value = quality
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun QualityPopup(onQualitySelected: (String) -> Unit) {
    if (openDialog.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
                .background(Color.Transparent)
        ) {
            AlertDialog(
                modifier = Modifier
                    .padding(top = 35.dp, bottom = 35.dp, start = 10.dp, end = 10.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(2.dp),
                        clip = true,
                        ambientColor = Color(0xFF000000),
                        spotColor = Color(0xFFC27E7E),
                    ),
                onDismissRequest = { openDialog.value = false },
                title = {
                    Text(
                        text = "Select Quality",
                        fontFamily = sans_bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = if (Q.size >= 3) {
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxSize()
                        } else {
                            Modifier
                                .wrapContentSize()
                        }
                    ) {
                        Text(
                            text = "Select the quality you want to watch",
                            fontFamily = sans_bold,
                            color = Color.White
                        )
                        // set vertical scroll state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Button(
                                onClick = {
                                    if (currentQualityUrl.value != "") {
                                        ActiveM3U8.value = currentQualityUrl.value
                                    }
                                },
                                modifier = Modifier
                                    .padding(1.dp)
                                    .padding(
                                        top = 4.dp,
                                    )
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xC436454F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(15.dp)
                            ) {

                                Text(text = "Auto", fontFamily = sans_bold)
                                if (currentQualityUrl.value == "") {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Settings",
                                        tint = Color.Yellow,
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height(30.dp)
                                            .padding(start = 10.dp)
                                    )
                                }
                            }
                            availQualities.value.reversed().forEach { quality ->
                                var attr = " (FHD)"
                                if (quality.quality.contains("1080")) {
                                    attr = " (FHD)"
                                } else if (quality.quality.contains("720")) {
                                    attr = " (HD)"
                                } else if (quality.quality.contains("480")) {
                                    attr = " (SD)"
                                } else if (quality.quality.contains("360")) {
                                    attr = " (LD)"
                                }
                                Button(
                                    onClick = {
                                        if (currentQualityUrl.value == "") {
                                            currentQualityUrl.value = ActiveM3U8.value
                                            ActiveM3U8.value = quality.url
                                        } else {
                                            ActiveM3U8.value = quality.url
                                        }
                                        openDialog.value = false
                                    },

                                    modifier = Modifier
                                        .padding(1.dp)
                                        .padding(
                                            top = 4.dp,
                                        )
                                        .fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xC436454F),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(15.dp)
                                ) {
                                    Text(
                                        text = quality.quality.split("x")[1] + "p$attr",
                                        fontFamily = sans_bold
                                    )
                                    if (quality.url == ActiveM3U8.value) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Settings",
                                            tint = Color.Yellow,
                                            modifier = Modifier
                                                .width(30.dp)
                                                .height(30.dp)
                                                .padding(start = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { openDialog.value = false },
                        modifier = Modifier
                            .padding(0.dp)
                            .padding(
                                top = 0.dp,
                            )
                            .clip(RoundedCornerShape(0.dp))
                            .shadow(
                                elevation = 0.dp,
                                shape = RoundedCornerShape(0.dp)
                            ),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF50057),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Confirm",
                            fontFamily = sans_bold
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { openDialog.value = false },
                        modifier = Modifier
                            .padding(0.dp)
                            .padding(
                                top = 0.dp,
                            ),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF651FFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cancel", fontFamily = sans_bold)
                    }
                },
                containerColor = Color(0xC4101314),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}

val trailerM3U8 = mutableStateOf("")

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayTrailerButton(player: ExoPlayer) {
    if (activeTitle.value?.trailer.isNullOrEmpty()) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 10.dp,
                start = 15.dp,
                end = 15.dp,
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1F1F1F))
            .clickable {

            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp,
                    horizontal = 5.dp
                )
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF1F1F1F))
                .clickable {
                    player.stop()
                    Thread {
                        showLoading.value = true
                        val trailer = FetchTrailer(activeTitle.value?.trailer ?: "")
                        if (trailer.isNotEmpty()) {
                            ActiveM3U8.value = trailer
                            showLoading.value = false
                        }
                    }.start()
                }
        ) {
            Text(
                text = "Play Trailer ~ ${activeTitle.value?.title}",
                fontFamily = sans_bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Globe Icon",
                tint = if (ActiveM3U8.value.contains("googlevideo.com")) {
                    Color.Red
                } else {
                    Color.Yellow
                },
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TitleMedata() {
    val ACT = activeTitle.value

    Column(
        modifier = Modifier
            .background(Color.Black)
    ) {
        val showShimmer = remember { mutableStateOf(true) }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 5.dp)
                .background(
                    Color.Black
                )
        ) {
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(ACT?.image)
                    .crossfade(true)
                    .build(),
                contentDescription = "Movie Poster",
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
                    .height(210.dp)
                    .width(130.dp),
                onSuccess = {
                    showShimmer.value = false
                }
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = ACT?.title ?: "",
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Row {
                    // if (imdbTT.year.end != "" && imdbTT.year.end != imdbTT.year.year && imdbTT.year.end != "0") {
                    Text(
                        text = ACT?.release ?: "",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    // } else {
                    //     Text(
                    //         text = imdbTT.year.year,
                    //         fontFamily = sans_bold,
                    //         color = Color.Gray,
                    //         fontSize = 12.sp
                    //    )
                    //}

                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFF1F1F1F))
                            .height(16.dp)
                            .padding(
                                horizontal = 3.dp,
                            )
                    )
                    {
                        Text(
                            text = ACT?.quality ?: "",
                            fontFamily = sans_bold,
                            color = Color.Gray,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                }
                Text(
                    text = ACT?.genres ?: "",
                    fontFamily = sans_bold,
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp)
                )
                {
                    Text(
                        text = "${ACT?.imdb_rating ?: "0"}/10",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(top = 1.dp, end = 3.dp)
                    )

                    for (i in 1..5) {
                        if (ACT != null) {
                            if (ACT.imdb_rating == "N/A") {
                                ACT.imdb_rating = "0"
                            }
                            if (i <= ((ACT.imdb_rating.toFloat().div(2)).toInt() ?: 0)) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_star),
                                    contentDescription = "Rating Star",
                                    modifier = Modifier
                                        .height(16.dp)
                                        .width(16.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_star_border),
                                    contentDescription = "Rating Star",
                                    modifier = Modifier
                                        .height(16.dp)
                                        .width(16.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
                if (ACT != null) {
                    Text(
                        text = ls(ACT.description, 400),
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
                val stars = ACT?.casts ?: ""
                if (stars.length > 55) {
                    Text(
                        text = "Starring: ${stars.substring(0, 55)}...",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
                    )
                } else if (stars.isNotEmpty()) {
                    Text(
                        text = "Starring: $stars",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 30.dp,
                    vertical = 10.dp
                )
                .padding(
                    bottom = 10.dp
                )
                .background(Color.Black),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp, vertical = 15.dp)
                        .clickable { },
                    contentScale = ContentScale.Crop,
                    colorFilter =
                    ColorFilter.lighting(
                        add = Color(0xFFFFFFFF),
                        multiply = Color.White
                    )
                )
                Text(
                    text = "My List",
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            }
            Column(
                modifier = Modifier
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp, vertical = 15.dp)
                        .clickable { },
                    contentScale = ContentScale.Crop,
                    colorFilter =
                    ColorFilter.lighting(
                        add = Color(0xFFFFFFFF),
                        multiply = Color.White
                    )
                )
                Text(
                    text = "Rate",
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            }
            Column(
                modifier = Modifier
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp, vertical = 15.dp)
                        .clickable { },
                    contentScale = ContentScale.Crop,
                    colorFilter =
                    ColorFilter.lighting(
                        add = Color(0xFFFFFFFF),
                        multiply = Color.White
                    )
                )
                Text(
                    text = "Share",
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            }
        }
        Divider(
            color = Color.DarkGray,
            modifier = Modifier
                .height(1.dp)
        )

        //SetupSeasonAndEpisodeSelector()
    }
}

var selectedSeasonName = mutableStateOf("")

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SetupSeasonAndEpisodeSelector(player: ExoPlayer) {
    if ((activeTitle.value?.category ?: "") != "tv") {
        return
    }
    val seasons = activeTitle.value?.seasons
    if (seasons.isNullOrEmpty()) {
        selectedSeasonName.value = "Loading..."
    } else {
        selectedSeasonName.value = seasons[0].title
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(
                top = 10.dp,
                start = 15.dp,
                end = 30.dp,
            )
    ) {
        Text(
            text = "Seasons and Episodes",
            fontFamily = sans_bold,
            color = Color.White,
            fontSize = 16.sp
        )

        if (seasons == null) {
            return
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1F1F1F))
                .padding(
                    horizontal = 20.dp,
                    vertical = 4.dp
                )
                .padding(
                    start = 2.dp,
                    end = 2.dp,
                )
        ) {
            var expanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = true
                    }
                    .background(Color.Transparent)
            ) {
                Text(
                    text = selectedSeasonName.value,
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(
                            horizontal = 2.dp,
                            vertical = 4.dp
                        )
                        .padding(
                            top = 2.dp,
                        )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Globe Icon",
                    tint = Color.Red,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                )

                DropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 0.dp
                        ),
                    properties = PopupProperties(clippingEnabled = false)
                ) {
                    seasons.forEach { s ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = s.title,
                                    fontFamily = sans_bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            },
                            onClick = {
                                activeSE.value = Pair(s.season_id.toInt(), 0)
                                selectedSeasonName.value = s.title
                                GatherEpisodes(
                                    activeTitle,
                                    activeSE,
                                    ActiveM3U8,
                                    Subs,
                                    showLoading,
                                    showNoSource,
                                    false
                                )
                                expanded = false
                            },
                            modifier = Modifier
                                .background(Color.Transparent),
                            contentPadding = PaddingValues(20.dp, 2.dp)
                        )
                        if (s != seasons.last()) {
                            Divider(
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .height(1.dp)
                                    .padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        println("Active SE: ${activeSE.value}, Episodes: ${activeTitle.value?.episodes?.size}")

        val pageSize = 5

        if ((activeTitle.value?.episodes?.size ?: 0) > 5) {
            LazyColumn(
                modifier =
                Modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 12.dp
                    )
                    .height(
                        if ((activeTitle.value?.episodes?.size ?: 0) > 5) {
                            500.dp
                        } else {
                            300.dp
                        }
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth(),
                state = rememberLazyListState()
            ) {
                itemsIndexed(activeTitle.value?.episodes ?: listOf()) { index, item ->
                    val pageIndex = index / pageSize
                    val pageStartIndex = pageIndex * pageSize
                    val pageEndIndex = (pageIndex + 1) * pageSize - 1

                    if (index in pageStartIndex..pageEndIndex) {
                        EpisodeItem(item, player)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun EpisodeItem(item: com.example.vflix.api.NEpisode, player: ExoPlayer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 6.dp,
                horizontal = 14.dp,
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1F1F1F))
            .clickable { }
    ) {
        var bgColor = Color(0xFF1F1F1F)
        if (item.episode_id.toInt() == activeSE.value.second) {
            bgColor = Color(0xFF2F2F2F)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp,
                    horizontal = 5.dp
                )
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable {
                    player.stop()
                    currentSeek.value = 0
                    showLoading.value = true
                    activeSE.value = Pair(activeSE.value.first, item.episode_id.toInt())
                    GatherEmbedURL(
                        activeTitle,
                        activeSE,
                        ActiveM3U8,
                        Subs,
                        showLoading,
                        showNoSource
                    )
                }
        ) {
            Text(
                text = ls(item.title, 30),
                fontFamily = sans_bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Globe Icon",
                tint = Color.Yellow,
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SetupSimilar(
    nav: NavHostController,
) {
    if (activeTitle.value?.similar_titles.isNullOrEmpty()) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(
                top = 10.dp,
                start = 15.dp,
                end = 10.dp,
            )
    ) {
        Text(text = "Similar Titles", fontFamily = sans_bold, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))
        val lazyListState = rememberLazyListState()

        val pageSize = 5

        LazyRow(
            modifier =
            Modifier
                .padding(
                    vertical = 2.dp,
                    horizontal = 12.dp
                )
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth(),
            state = lazyListState,
        ) {
            itemsIndexed(activeTitle.value?.similar_titles ?: listOf()) { index, item ->
                val pageIndex = index / pageSize
                val pageStartIndex = pageIndex * pageSize
                val pageEndIndex = (pageIndex + 1) * pageSize - 1

                if (index in pageStartIndex..pageEndIndex) {
                    if ((item.poster.length ?: 0) > 0) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                        ) {
                            val showShimmer = remember { mutableStateOf(true) }
                            Row(
                            ) {
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(item.poster)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Title Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height(158.dp)
                                        .width(110.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                5.dp
                                            )
                                        )
                                        .background(
                                            shimmerBrush(
                                                targetValue = 1300f,
                                                showShimmer = showShimmer.value
                                            )
                                        )
                                        .clickable {
                                            prevPageHistory.add(
                                                PrevNav(
                                                    prevID = clickedID,
                                                    prevName = clickedName,
                                                    prevMediaType = mediaType,
                                                    prevSearch = searchValue.value,
                                                    prevPage = "videoScreen",
                                                    se = currentSE.value,
                                                )
                                            )
                                            clickedID = item.href
                                            clickedName = item.title
                                            mediaType = item.category
                                            nav.navigate("videoScreen")
                                        },
                                    onSuccess = { showShimmer.value = false },
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier.width(
                                4.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

// ls: LimitString
fun ls(s: String, l: Int): String {
    return if (s.length > l) {
        s.substring(0, l) + "..."
    } else {
        s
    }
}