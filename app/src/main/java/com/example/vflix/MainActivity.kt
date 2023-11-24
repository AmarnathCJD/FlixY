package com.example.vflix

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.paint
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
import androidx.core.view.marginLeft
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.ParsingLoadable
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.example.vflix.auth.DotsPreview
import com.example.vflix.auth.db
import com.example.vflix.parser.FetchChannels
import com.example.vflix.ui.theme.sans_bold

var appUserId = "1151584573787594752"
var video_view: PlayerView? = null

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
            setScreenOrientation(0)
            ExoPlayerExample()
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
        db.get()
        Thread {
            Thread.sleep(500)
            showStartLogo = false
        }.start()

        Thread {
            FetchChannels()
        }.start()
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
        val playerView = findViewById<PlayerView>(R.id.playerView)
        if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        } else if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        } else if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
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
    }
}

val mpd = mutableStateOf("")

val Q = mutableListOf<Quality>()

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ExoPlayerExample() {
    val context = LocalContext.current
    //LaunchedEffect(Unit) {
    //  getMPD()
    //}

    m3u8ToQualities("https://prod-ent-live-gm.jiocinema.com/bpk-tv/Comedy_Central_HD_voot_MOB/Fallback/index.m3u8", Q)
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

    val mediaItem = HlsMediaSource.Factory(
        DefaultHttpDataSource.Factory()
    ).createMediaSource(
        androidx.media3.common.MediaItem.Builder()
            .setUri("https://prod-ent-live-gm.jiocinema.com/bpk-tv/Comedy_Central_HD_voot_MOB/Fallback/index.m3u8")
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



    AndroidView(factory = { context ->
        LayoutInflater.from(context).inflate(R.layout.activity_player, null)
    }, update = { view ->
        video_view = view.findViewById(R.id.playerView)
        video_view?.player = player
        video_view?.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        val zoomButton = view.findViewById<ImageView>(R.id.zoomButton)
        val rotateButton = view.findViewById<ImageView>(R.id.rotateButton)
        val playerView = view.findViewById<PlayerView>(R.id.playerView)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        playerView.setControllerVisibilityListener(
            PlayerControlView.VisibilityListener { visibility ->
                zoomButton.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
                rotateButton.visibility =
                    if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
            }
        )
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)


        playerView.showController()
    })
}


fun getMPD() {
    val client = okhttp3.OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url("https://live.csa.codes/api/drm?id=962")
        .build()

    client.newCall(request).enqueue(
        object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("failed to get mpd")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                if (body != null) {
                    val json = org.json.JSONObject(body)
                    val mpd = json.getString("mpd")
                    println("mpd: $mpd")
                }
            }
        }
    )
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
                painter = painterResource(id = R.drawable.pxfuel__2_),
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

fun m3u8ToQualities(m3u8_url: String, q: MutableList<Quality>) {
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
                                q.add(Quality(name, m3u8_url.split("index.m3u8")[0] + url))
                            }
                        }
                    }
                }

                println("XQualities: $q")
            }
        }
    )
}