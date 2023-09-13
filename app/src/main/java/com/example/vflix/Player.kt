package com.example.vflix

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.ui.theme.sans_bold
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

val currentSE =  mutableStateOf(Pair(1, 1)) // Season, Episode

data class PlayerError(
    var show: Boolean = false,
    var message: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoScreen(
    nav: NavHostController = rememberNavController(),
) {
    val hlsSource = remember { mutableStateOf(CDN(listOf(), listOf(), "")) }
    val isF = remember { mutableStateOf(false) }
    val playbackStopped = remember { mutableStateOf(false) }
    val playerErr = remember { mutableStateOf(PlayerError()) }
    val imdbIdState = remember { mutableStateOf(clickedID) }
    val showSpinner = remember { mutableStateOf(true) }

    val imdbTTState = remember {
        mutableStateOf(
            ImdbTitle(
                "",
                "",
                "",
                Year("", ""),
                Runtime(""),
                Rating(0f),
                Poster("", 0, 0),
                listOf(),
                "",
                listOf(),
                listOf(),
                listOf(),
            )
        )
    }

    val imdbTT = imdbTTState.value
    var contentType = imdbTT.type
    val context = LocalContext.current
    val trackSelector = DefaultTrackSelector(context)

    LaunchedEffect(Unit) {
        fetchIMDBTitle(imdbID = clickedID, imdbTTState, mediaType, imdbIdState)
    }

    val player: ExoPlayer = remember {
        ExoPlayer.Builder(context).setTrackSelector(trackSelector)
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
                                text = "Now Playing: ",
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

        Column(
            modifier = if (isF.value) {
                println("Filling max size")
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
        )
        {

            if (showSpinner.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp),
                        color = Color.Red,
                        strokeWidth = 6.dp
                    )
                }
            }
            if (playerErr.value.show) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerErr.value.message,
                        fontFamily = sans_bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1F1F1F))
                            .padding(12.dp)
                    )
                }
            }
            if (mediaType != "" && !clickedID.contains("tt")) {
                contentType = if (mediaType.contains("tv")) {
                    "TV Show"
                } else {
                    "Movie"
                }
            }
            if (!contentType.isNullOrEmpty() && imdbIdState.value != "") {
                if (hlsSource.value.sources.isEmpty()) {
                    fetchSourceFromIMDBID(
                        imdbIdState.value,
                        hlsSource,
                        contentType,
                        currentSE.value.first,
                        currentSE.value.second,
                        playerErr,
                        showSpinner
                    )
                }
                if (hlsSource.value.getBest() != "") {
                    showSpinner.value = false
                    VideoPlayer(player, hlsSource.value.getBest(), isF, playbackStopped, context)
                }
            }

        }
        if (!isF.value && !imdbTTState.value.id.isNullOrEmpty()) {
            MediaMetadata(imdbTTState)
        }

        if (!isF.value && !hlsSource.value.tmdbID.isNullOrEmpty()) {
            if (contentType.contains("TV")) {
                SeasonAndEpisodeSelector(
                    hlsSource.value.tmdbID,
                    currentSE,
                    hlsSource,
                    playerErr,
                    showSpinner
                )
            }
            if (!imdbTTState.value.id.isNullOrEmpty()) {
                SetSimilarTitles(
                    imdbTTState.value.similarTitles,
                    nav,
                    player
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    player: ExoPlayer,
    hslSource: String,
    isF: MutableState<Boolean>,
    playbackStopped: MutableState<Boolean>,
    context: Context
) {
    val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    val hlsMediaFactory =
        HlsMediaSource.Factory(dataSourceFactory)
    val hlsMediaSource = hlsMediaFactory.createMediaSource(MediaItem.fromUri(hslSource))

    player.addListener(
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                println("Player error: $error")
                player.stop()
            }
        }
    )

    player.trackSelectionParameters =
        player.trackSelectionParameters
            .buildUpon()
            .setMaxVideoSize(1920, 1080)
            .setPreferredAudioLanguage("hu")
            .build()


    AndroidView(
        factory = {
            PlayerView(context).apply {
                this.player = player
                player.setMediaSource(hlsMediaSource)
                player.playWhenReady = true
                setShowSubtitleButton(true)

                if (playbackStopped.value) {
                    player.stop()
                }

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
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 0.dp, 0.dp)
            .background(Color.Black)
    )
}

data class MediaSrc(
    @com.google.gson.annotations.SerializedName("quality") val quality: String,
    @com.google.gson.annotations.SerializedName("url") val url: String
)

data class Subtitle(
    val lang: String,
    val url: String
)

data class CDN(
    @com.google.gson.annotations.SerializedName("media") val sources: List<MediaSrc>,
    val subtitles: List<Subtitle>,
    @com.google.gson.annotations.SerializedName("tmdb_id") val tmdbID: String,
)

fun CDN.getBest(): String {
    var best = ""
    var bestQuality = 0
    for (source in this.sources) {
        if (source.quality != "auto") {
            if (source.quality.toInt() > bestQuality) {
                best = source.url
                bestQuality = source.quality.toInt()
            }
        }
    }
    return best
}

fun fetchSourceFromIMDBID(
    imdbID: String,
    src: MutableState<CDN>,
    type: String,
    season: Int = 1,
    episode: Int = 1,
    playerErr: MutableState<PlayerError>,
    showSpinner: MutableState<Boolean>,
    RETRY: Int = 0
) {
    var url = "https://meh.com?imdb_id=$imdbID"
    if (type.contains("TV")) {
        url += "&s=$season&e=$episode"
    }

    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        if (json.contains("imdb_id is")) {
                            return
                        }
                    }
                    try {
                        val movies = Gson().fromJson(json, CDN::class.java)
                        if (movies.sources.isNullOrEmpty()) {
                            if (RETRY < 1) {
                                fetchSourceFromIMDBID(
                                    imdbID,
                                    src,
                                    type,
                                    season,
                                    episode,
                                    playerErr,
                                    showSpinner,
                                    RETRY + 1
                                )
                                return
                            } else {
                                playerErr.value = PlayerError(
                                    show = true,
                                    message = "No sources!!!"
                                )
                                showSpinner.value = false
                                return
                            }
                        }
                        src.value = movies
                    } catch (e: Exception) {
                        println("Error, ${e.message}")
                    }
                }
            }
        )
}

fun Activity.lockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
}

fun Activity.unlockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this.findActivity() ?: return
    activity.requestedOrientation = orientation
}

@Composable
fun MediaMetadata(imdbTTState: MutableState<ImdbTitle>) {
    val imdbTT = imdbTTState.value

    Column(
        modifier = Modifier
            //.fillMaxSize()
            .background(Color.Black)
    ) {
        val showShimmer = remember { mutableStateOf(true) }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 5.dp)
        ) {
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(imdbTT.poster.url)
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
                    .height(210.dp)
                    .width(130.dp),
                onSuccess = {
                    showShimmer.value = false
                }
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = imdbTT.title,
                    fontFamily = sans_bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Row {
                    if (imdbTT.year.end != "" && imdbTT.year.end != imdbTT.year.year && imdbTT.year.end != "0") {
                        Text(
                            text = "${imdbTT.year.year} - ${imdbTT.year.end}",
                            fontFamily = sans_bold,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = imdbTT.year.year,
                            fontFamily = sans_bold,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

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
                            text = imdbTT.runtime.runtime,
                            fontFamily = sans_bold,
                            color = Color.Gray,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    imdbTT.countriesOfOrigin.forEach { country ->
                        Text(
                            text = country,
                            fontFamily = sans_bold,
                            color = Color.Gray,
                            fontSize = 12.sp,
                        )
                    }
                }
                Text(
                    text = imdbTT.genres.joinToString(),
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
                        text = "${imdbTT.rating.value}/10",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(top = 1.dp, end = 3.dp)
                    )

                    for (i in 1..5) {
                        if (i <= (imdbTT.rating.value.div(2)).toInt()) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_star),
                                contentDescription = "Mm",
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(16.dp),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_star_border),
                                contentDescription = "Mm",
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(16.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
                Text(
                    text = imdbTT.plot,
                    fontFamily = sans_bold,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
                val stars = imdbTT.stars.joinToString { it.name }
                if (stars.length > 55) {
                    Text(
                        text = "Starring: ${stars.substring(0, 55)}...",
                        fontFamily = sans_bold,
                        color = Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
                    )
                } else {
                    Text(
                        text = "Starring: ${imdbTT.stars.joinToString { it.name }}",
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
    }
}

data class Year(
    val year: String,
    val end: String,
)

data class Runtime(
    @com.google.gson.annotations.SerializedName("plain_text") val runtime: String,
)

data class Rating(
    val value: Float,
)

data class Star(
    val name: String,
)

data class SimilarTitle(
    val id: String,
    val title: String,
    val poster: String,
    val type: String,
)

data class ImdbTitle(
    val id: String,
    val title: String,
    val type: String,
    @com.google.gson.annotations.SerializedName("release_year") val year: Year,
    val runtime: Runtime,
    var rating: Rating,
    @com.google.gson.annotations.SerializedName("Poster") val poster: Poster,
    var genres: List<String>,
    var plot: String,
    var stars: List<Star>,
    @com.google.gson.annotations.SerializedName("countries_of_origin") var countriesOfOrigin: List<String>,
    @com.google.gson.annotations.SerializedName("more_like_this") var similarTitles: List<SimilarTitle>,
)

data class TmdbResp(
    @com.google.gson.annotations.SerializedName("imdb_id") val imdbID: String,
)

fun fetchIMDBTitle(
    imdbID: String,
    imdbTT: MutableState<ImdbTitle>,
    mediaType: String = "movie",
    imdbIdState: MutableState<String>
) {
    if (!imdbID.contains("tt")) {
        val url =
            "https://api.themoviedb.org/3/$mediaType/$imdbID/external_ids?api_key=d56e51fb77b081a9cb5192eaaa7823ad"
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        client
            .newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("Error, ${e.message}")
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        val title = Gson().fromJson(json, TmdbResp::class.java)
                        imdbIdState.value = title.imdbID
                        var mediaTypeParam = "movie"
                        if (!mediaType.isNullOrEmpty()) {
                            mediaTypeParam = mediaType
                        }
                        fetchIMDBTitle(title.imdbID, imdbTT, mediaTypeParam, imdbIdState)
                    }
                }
            )
    }
    val url = "https://a.ztorr.me/api/imdb?id=$imdbID"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        if (json.contains("imdb_id is")) {
                            return
                        }
                    }
                    val title = Gson().fromJson(json, ImdbTitle::class.java)
                    imdbTT.value = title
                }
            }
        )
}


@Composable
fun SeasonAndEpisodeSelector(
    tmdbId: String,
    se: MutableState<Pair<Int, Int>>,
    hlsSource: MutableState<CDN>,
    playerErr: MutableState<PlayerError>,
    showSpinner: MutableState<Boolean>,
) {
    val seasons = remember { mutableStateOf(listOf<SeasonMin>()) }
    val episodes = remember { mutableStateOf(listOf<Episode>()) }
    val selectedSeason = remember { mutableStateOf(1) }
    val selectedSeasonName = remember { mutableStateOf("Season 1") }

    LaunchedEffect(key1 = tmdbId) {
        fetchSeasons(tmdbId, seasons)
    }

    LaunchedEffect(key1 = tmdbId, key2 = selectedSeason.value) {
        fetchEpisodes(tmdbId, selectedSeason.value, episodes)
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
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1F1F1F))
                .padding(
                    horizontal = 4.dp,
                    vertical = 5.dp
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
                    tint = Color.White,
                    modifier = Modifier.padding(
                        horizontal = 2.dp,
                        vertical = 4.dp
                    )
                )
                DropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color(0xFF292828))
                        .padding(
                            top = 0.dp,
                            bottom = 0.dp,
                        )
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    seasons.value.forEach { s ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = s.name,
                                    fontFamily = sans_bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            },
                            onClick = {
                                selectedSeason.value = s.num
                                selectedSeasonName.value = s.name
                                expanded = false
                            },
                            modifier = Modifier
                                .background(Color(0xFF1F1F1F))
                        )
                        if (s != seasons.value.last()) {
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
        Spacer(modifier = Modifier.height(6.dp))
        val lazyListState = rememberLazyListState()

        var pageSize = episodes.value.size / 3
        if (pageSize == 0) {
            pageSize = 1
        }
        if (!episodes.value.isNullOrEmpty()) {
            LazyColumn(
                modifier =
                Modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 12.dp
                    )
                    .height(
                        if (episodes.value.size > 3) {
                            700.dp
                        } else {
                            350.dp
                        }
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth(),
                state = lazyListState,
            ) {
                itemsIndexed(episodes.value) { index, item ->
                    val pageIndex = index / pageSize
                    val pageStartIndex = pageIndex * pageSize
                    val pageEndIndex = (pageIndex + 1) * pageSize - 1

                    if (index in pageStartIndex..pageEndIndex) {
                        if (!item.stillPath.isNullOrEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                            ) {
                                val showShimmer = remember { mutableStateOf(true) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model =
                                        ImageRequest.Builder(LocalContext.current)//"https://image.tmdb.org/t/p/w1280"
                                            .data("https://image.tmdb.org/t/p/w500" + item.stillPath)//item.poster?.url ?: TEST_IMAGE_URLS[0])
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Movie Poster",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(90.dp)
                                            .width(160.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .clickable {
                                                se.value = Pair(
                                                    selectedSeason.value,
                                                    item.num
                                                )

                                                hlsSource.value = CDN(
                                                    sources = listOf(),
                                                    subtitles = listOf(),
                                                    tmdbID = tmdbId
                                                )
                                                showSpinner.value = true
                                                fetchSourceFromIMDBID(
                                                    item.showID,
                                                    hlsSource,
                                                    "TV Show",
                                                    selectedSeason.value,
                                                    item.num,
                                                    playerErr,
                                                    showSpinner
                                                )
                                            }
                                            .background(
                                                shimmerBrush(
                                                    targetValue = 1300f,
                                                    showShimmer = showShimmer.value
                                                )
                                            ),
                                    )
                                    Column {
                                        Text(
                                            text = "${index + 1}. ${item.name}",
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 12.dp
                                                )
                                                .padding(
                                                    top = 6.dp,
                                                    bottom = 1.dp
                                                )
                                                .fillMaxWidth(),
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                fontFamily = sans_bold
                                            )
                                        )

                                        Text(
                                            text = "${item.runtime}m",
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 12.dp
                                                )
                                                .padding(
                                                    top = 1.dp,
                                                    bottom = 6.dp
                                                )
                                                .fillMaxWidth(),
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontFamily = sans_bold,
                                                fontWeight = FontWeight.ExtraLight
                                            )
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.overview,
                                    modifier = Modifier
                                        .padding(
                                            vertical = 6.dp,
                                        )
                                        .fillMaxWidth(),
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        fontFamily = sans_bold,
                                        fontWeight = FontWeight.ExtraLight
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

data class SeasonMin(
    val id: String,
    val name: String,
    @com.google.gson.annotations.SerializedName("season_number") val num: Int,
)

data class Episode(
    val id: String,
    val name: String,
    val runtime: Int,
    @com.google.gson.annotations.SerializedName("episode_number") val num: Int,
    val overview: String,
    @com.google.gson.annotations.SerializedName("season_number") val season: Int,
    @com.google.gson.annotations.SerializedName("show_id") val showID: String,
    @com.google.gson.annotations.SerializedName("still_path") val stillPath: String,
    @com.google.gson.annotations.SerializedName("vote_average") val voteAverage: Float,
)

data class TmdbEpisodes(
    val episodes: List<Episode>,
)

data class TmdbSeasons(
    val seasons: List<SeasonMin>,
)

fun fetchSeasons(tmdbId: String, seasons: MutableState<List<SeasonMin>>) {
    val url = "https://api.themoviedb.org/3/tv/$tmdbId?api_key=d56e51fb77b081a9cb5192eaaa7823ad"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        if (json.contains("imdb_id is")) {
                            return
                        }
                    }
                    val title = Gson().fromJson(json, TmdbSeasons::class.java)
                    seasons.value = title.seasons
                }
            }
        )
}

fun fetchEpisodes(tmdbId: String, season: Int, episodes: MutableState<List<Episode>>) {
    val url =
        "https://api.themoviedb.org/3/tv/$tmdbId/season/$season?api_key=d56e51fb77b081a9cb5192eaaa7823ad"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        if (json.contains("imdb_id is")) {
                            return
                        }
                    }
                    val title = Gson().fromJson(json, TmdbEpisodes::class.java)
                    episodes.value = title.episodes
                }
            }
        )
}

@Composable
fun SetSimilarTitles(
    similarTitles: List<SimilarTitle>,
    nav: NavHostController,
    player: ExoPlayer
) {
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

        var pageSize = similarTitles.size / 3
        if (pageSize == 0) {
            pageSize = 1
        }

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
            itemsIndexed(similarTitles) { index, item ->
                val pageIndex = index / pageSize
                val pageStartIndex = pageIndex * pageSize
                val pageEndIndex = (pageIndex + 1) * pageSize - 1

                if (index in pageStartIndex..pageEndIndex) {
                    if ((item.poster?.length ?: 0) > 0) {
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
                                            player.stop()
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
                                            clickedID = item.id
                                            clickedName = item.title
                                            mediaType = if (item.type == "movie") {
                                                "Movie"
                                            } else {
                                                "TV Show"
                                            }
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
    Spacer(modifier = Modifier.height(14.dp))
    BottomAppBar(
        //elevation = FloatingActionButtonDefaults.elevation(
        //defaultElevation = 4.dp,
        //),
        containerColor = Color.Transparent
    )
    //onClick = { navController.navigate("searchPanel") },
    {
        Row(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier =
                Modifier

                    .background(Color.Black),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    Image(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(
                                top = 8.dp,
                            )
                            .clickable {
                                player.stop()
                                prevPageHistory.clear()
                                nav.navigate("homePage")
                            },
                        contentScale = ContentScale.Crop,
                        colorFilter =
                        ColorFilter.lighting(
                            add = Color(0xFFFFFFFF),
                            multiply = Color.White
                        )
                    )
                    Text(
                        text = "Home",
                        fontSize = 8.sp,
                        fontFamily = sans_bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(
                                bottom = 8.dp,
                            )
                    )
                }
            }
            Row(
                modifier =
                Modifier

                    .background(Color.Black),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(
                                top = 8.dp,
                            )
                            .clickable {
                                player.stop()
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
                                nav.navigate("searchPanel") },
                        contentScale = ContentScale.Crop,
                        colorFilter =
                        ColorFilter.lighting(
                            add = Color(0xFFFFFFFF),
                            multiply = Color.White
                        )
                    )
                    Text(
                        text = "Search",
                        fontSize = 8.sp,
                        fontFamily = sans_bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(
                                bottom = 8.dp,
                            )
                    )
                }
            }

            Row(
                modifier =
                Modifier

                    .background(Color.Black),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            //
                            .padding(horizontal = 8.dp)
                            .padding(
                                top = 8.dp,
                            )
                            //.clickable { nav.navigate("searchPanel") },
                        ,contentScale = ContentScale.Crop,
                        colorFilter =
                        ColorFilter.lighting(
                            add = Color(0xFFFFFFFF),
                            multiply = Color.White
                        )
                    )
                    Text(
                        text = "Account",
                        fontSize = 8.sp,
                        fontFamily = sans_bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(
                                bottom = 8.dp,
                            )
                    )
                }

            }
        }
    }
}

data class TmdbTitleMin(
    val id: String,
    val title: String,
    var name: String,
    var media_type: String,
    @com.google.gson.annotations.SerializedName("poster_path") val poster: String,
)

data class TmdbSimilar(
    val results: List<TmdbTitleMin>,
)

fun fetchSimilarTitles(
    tmdbId: String,
    similarTitles: MutableState<List<TmdbTitleMin>>,
    mediaType: String
) {
    val page = Random.nextInt(1, 10)
    val url =
        "https://api.themoviedb.org/3/$mediaType/$tmdbId/similar?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=$page"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {
                        if (json.contains("imdb_id is")) {
                            return
                        }
                    }
                    val title = Gson().fromJson(json, TmdbSimilar::class.java)
                    similarTitles.value = title.results
                }
            }
        )
}