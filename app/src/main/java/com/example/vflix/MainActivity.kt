package com.example.vflix

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vflix.auth.DotsPreview
import com.example.vflix.ui.theme.sans_bold

var appUserId = "1151584573787594752"
var video_view: PlayerView? = null
val openDialog = mutableStateOf(false)
val currentMPD = mutableStateOf("")

val hideChannelChooser = mutableStateOf(false)


class MainActivity : ComponentActivity() {
    private var scaleGestureDetector: ScaleGestureDetector? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            /*   var navController = rememberNavController()

          /*  clickedID = "tt4574334"
            clickedName = "Super 30"
            mediaType = "tv"
            VideoScreen(nav = navController)*/
            NavHost(navController, startDestination = "startApp") {
                composable(route = "startApp") {
                    StartApp(navController)
                }
                composable(route = "homePage") {
                    HomePage(navController)
                }
                composable(route = "searchPanel") {
                    EnterAnimation {
                        SearchPanel(navController)
                    }
                }
                composable(route = "videoScreen") {
                    EnterAnimation {
                        VideoScreen(
                            nav = navController,
                        )
                    }
                }
                composable(route = "loginPage") {
                    LoginForm(nav = navController)
                }
                composable(route = "liveTV") {
                    LiveTV(nav = navController)
                }
            }
        }
        */
            //clickedID = "/tv/avatar-the-last-airbender-38893"
            //clickedName = "Avatar: The Last Airbender"
            //mediaType = "tv"
            val navController = rememberNavController()
            NavHost(navController, startDestination = "homePage") {
                composable(route = "homePage") {
                    EnterAnimation {
                        HomePage(navController = navController)
                    }
                }
                composable(route = "videoScreen") {
//                    EnterAnimation {
//                        PlayerN(nav = navController)
//                    }

                    LandingVideoPage(nav = navController)
                }
                composable(route = "searchPanel") {
                    EnterAnimation {
                        SearchPanel(navController)
                    }
                }
                composable(route = "videoPlayerScreen") {
                    PlayerN(nav = navController)
                }
            }
            //  ExoPlayerExample()
            //QualityPopup(onQualitySelected = { quality ->
            //  println("quality selected: $quality")
            //})
            //QualityPopup(onQualitySelected = { quality ->
            //  println("quality selected: $quality")
            //})


        }


        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            colFactor.intValue = 6
        } else {
            colFactor.intValue = 3
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        if (event != null) {
            println("touch event")
            scaleGestureDetector?.onTouchEvent(event)
        }

        return true
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun onZoomButtonClick(view: View) {
        println("zoom button clicked")
        val playerView = findViewById<PlayerView>(R.id.playerView)
        if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    // TODO: fix left align issue

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun onRotateButtonClick(view: View) {
        val currentRotation = windowManager.defaultDisplay.rotation
        requestedOrientation = if (currentRotation == android.view.Surface.ROTATION_0) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (currentRotation == android.view.Surface.ROTATION_0) {
            isF.value = true
        } else {
            isF.value = false
        }
    }

    fun onQualityButtonClick(view: View) {
        openDialog.value = true
    }
}

data class Channel(val name: String, val url: String)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun channelChooser() {
    if (!hideChannelChooser.value) {
        val channels = listOf(
            Channel("HBO", "https://ctrl.laotv.la/live/HBO/index.m3u8"),
            Channel(
                "Jio Cinema",
                "https://prod-ent-live-gm.jiocinema.com/hls/live/2099795/hd_akamai_iosmob_avc_mal_indvsaus_t2001/master.m3u8"
            ),
            Channel(
                "Comedy Central",
                "https://prod-ent-live-gm.jiocinema.com/bpk-tv/Comedy_Central_HD_voot_MOB/Fallback/index.m3u8"
            ),
            Channel("Star Movies", "https://ctrl.laotv.la/live/StarMovie/index.m3u8"),
            Channel(
                "Colors HD",
                "https://prod-ent-live-gm.jiocinema.com/bpk-tv/Colors_HD_voot_MOB/Fallback/index.m3u8"
            ),
            Channel("AXN", "https://m1.ballnaja.com/live/AXN/playlist.m3u8"),
            Channel("Cinemax", "https://ctrl.laotv.la/live/Cinemax/index.m3u8"),
            Channel(
                "Movies",
                "https://shls-live-ak.akamaized.net/out/v1/90143f040feb40589d18c57863d9e829/index.m3u8"
            ),
            Channel(
                "Colors Infinity",
                "https://prod-ent-live-gm.jiocinema.com/bpk-tv/Colors_Infinity_HD_voot_MOB/Fallback/index.m3u8"
            ),
            Channel(
                "Asianet HD",
                "https://mprvod01.neestream.net/anandmedia/asianethd_576/index.m3u8"
            ),
            Channel(
                "Star World",
                "https://prod-ent-live-gm.jiocinema.com/bpk-tv/Star_World_HD_voot_MOB/Fallback/index.m3u8"
            ),
            Channel(
                "Asianet Movies",
                "https://mprvod01.neestream.net/anandmedia/asianetmovies_576/index.m3u8"
            ),
            Channel(
                "Asianet Plus",
                "https://mprvod01.neestream.net/anandmedia/asianetplus_576/index.m3u8"
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(top = 30.dp, bottom = 30.dp, start = 30.dp, end = 30.dp)
        ) {
            AlertDialog(
                modifier = Modifier
                    .padding(top = 25.dp, bottom = 25.dp, start = 10.dp, end = 10.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                        clip = true,
                        ambientColor = Color(0xFF000000),
                        spotColor = Color(0xFFC27E7E),
                    ),
                onDismissRequest = { openDialog.value = false },
                title = { Text(text = "Select Channel", fontFamily = sans_bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Select the channel you want to watch",
                            fontFamily = sans_bold
                        )
                        channels.forEach { channel ->
                            Button(
                                onClick = {
                                    currentMPD.value = channel.url
                                    currentQualityUrl.value = channel.url
                                    openDialog.value = false
                                    hideChannelChooser.value = true
                                },

                                modifier = Modifier
                                    .padding(1.dp)
                                    .padding(
                                        top = 4.dp,
                                    )
                                    .fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1F1F1F),
                                    contentColor = Color.White
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                            ) {
                                Text(text = channel.name, fontFamily = sans_bold)
                                if (channel.url == currentMPD.value) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color(0xFF651FFF),
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height(30.dp)
                                            .padding(start = 10.dp)
                                    )
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
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                            .shadow(
                                elevation = 0.dp,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                            ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF651FFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cancel", fontFamily = sans_bold)
                    }
                },
                containerColor = Color(0xC4B5B2BB),
            )
        }
    }
}



val Q = mutableListOf<Quality>()

@RequiresApi(Build.VERSION_CODES.Q)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ExoPlayerExample() {
    if (!hideChannelChooser.value) {
        channelChooser()
    } else {
        val context = LocalContext.current
        context.setScreenOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        //currentMPD.value = "https://ctrl.laotv.la/live/HBO/index.m3u8"
        if (currentQualityUrl.value == "") {
            currentQualityUrl.value = currentMPD.value
        }

        if (Q.size == 0) {
            m3u8ToQualities(
                currentMPD.value,

            )
        }

        if (Q.size > 0 && currentQualityUrl.value == "") {
            currentQualityUrl.value = Q[Q.size - 1].url
        }
        val trackSelector =
            remember { androidx.media3.exoplayer.trackselection.DefaultTrackSelector(context) }
        val player = remember {
            ExoPlayer.Builder(context).setTrackSelector(trackSelector)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(
                        DefaultDataSourceFactory(
                            context,
                            DefaultHttpDataSource.Factory()
                        )
                    )
                )
                .build()
        }

        BackHandler {
            if (openDialog.value) {
                openDialog.value = false
            } else {
                player.stop()
                context.setScreenOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                hideChannelChooser.value = false
            }
        }

        if (currentQualityUrl.value != "") {
            val mediaItem = HlsMediaSource.Factory(
                DefaultHttpDataSource.Factory()
            ).createMediaSource(
                androidx.media3.common.MediaItem.Builder()
                    .setUri(currentQualityUrl.value)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .setSubtitles(
                        listOf(
                            androidx.media3.common.MediaItem.Subtitle(
                                Uri.parse("https://prod-ent-live-gm.jiocinema.com/bpk-tv/Comedy_Central_HD_voot_MOB/Fallback/index.m3u8"),
                                MimeTypes.APPLICATION_M3U8,
                                "en"
                            )
                        )
                    )
                    .setSubtitleConfigurations(
                        listOf(
                            androidx.media3.common.MediaItem.SubtitleConfiguration.Builder(
                                Uri.parse("https://raw.githubusercontent.com/andreyvit/subtitle-tools/master/sample.srt")
                            )
                                .setLanguage(
                                    "en"
                                )
                                .setLabel(
                                    "English"
                                )
                                .build()
                        )
                    )
                    .build()
            )



            player.setMediaSource(mediaItem)

            player.prepare()
            player.play()
        }



        AndroidView(factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.activity_player, null)
        }, update = { view ->
            video_view = view.findViewById(R.id.playerView)
            video_view?.player = player
            video_view?.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            val zoomButton = view.findViewById<ImageView>(R.id.zoomButton)
            val rotateButton = view.findViewById<ImageView>(R.id.rotateButton)
            val playerView = view.findViewById<PlayerView>(R.id.playerView)
            val qualityButton = view.findViewById<ImageView>(R.id.qualityButton)
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
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
        })

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
    }
}




var showStartLogo by mutableStateOf(true)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StartApp(nav: NavHostController) {
    HomePage(navController = nav)
    if (showStartLogo) {
        StartLogo()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StartLogo() {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.logo),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            .zIndex(3f),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

        ) {
        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "V",
                    fontFamily = sans_bold,
                    fontSize = 90.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE50914),
                        shadow = Shadow(
                            color = Color(0xFF3C0202),
                            blurRadius = 80f,
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    ),
                    color = Color(0xFFAC1559),
                    modifier = androidx.compose.ui.Modifier
                        .padding(bottom = 20.dp)
                )
                DotsPreview()
            }
        }
    }
}

data class Quality(val name: String, val url: String)

fun m3u8ToQualities(m3u8_url: String) {
    val client = okhttp3.OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(m3u8_url)
        .build()

    client.newCall(request).enqueue(
        object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("failed to get mpd")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                if (body != null) {
                    val lines = body.split("\n")
                    for (line in lines) {
                        if (line.contains("RESOLUTION")) {
                            val name = line.split("RESOLUTION=")[1].split(",")[0]
                            val url = lines[lines.indexOf(line) + 1]
                            if (!url.contains("#EXT-X") && url.contains(".m3u8")) {
                                println("Quality: $name, ${m3u8_url.split("index.m3u8")[0] + url}")
                                //q.add(Quality(name, m3u8_url.split("index.m3u8")[0] + url))
                            }
                        }
                    }
                }

                //println("XQualities: $q")
            }
        }
    )
}