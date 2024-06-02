package com.example.vflix

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.api.CWAlreadyRead
import com.example.vflix.api.ContinueWatching
import com.example.vflix.api.IsServerOffline
import com.example.vflix.api.retrieveContinueWatchingFromInternalStorage
import com.example.vflix.auth.Auth
import com.example.vflix.auth.Watched
import com.example.vflix.parser.GetRandGradient
import com.example.vflix.ui.theme.sans_bold
import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.absoluteValue
import com.example.vflix.api.FeaturedItem as featuredItem
import com.example.vflix.api.FeaturedItems as featuredItems
import com.example.vflix.api.GatherFeaturedItems as gatherFeaturedItems

data class PrevNav(
    var prevPage: String,
    var prevID: String,
    var prevName: String,
    var prevMediaType: String,
    var prevSearch: String,
    var se: Pair<Int, Int> = Pair(1, 1),
)

var prevPageHistory = mutableListOf<PrevNav>()
var clickedName = "Stranger Things"
var clickedID = "/tv/stranger-things-39444"
var mediaType = "TV"


@Composable
fun EnterAnimation(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visibleState = MutableTransitionState(
            initialState = false
        ).apply { targetState = true },
        modifier = Modifier,
        enter = slideInVertically(
            initialOffsetY = { -400 }
        ) + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(initialAlpha = 0.9f),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavHostController) {

    val context = LocalContext.current
    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        topBar = { TopBar(navController = navController) },
    ) { innerPadding ->
        val configuration = LocalConfiguration.current

        LaunchedEffect(Unit) {
            gatherFeaturedItems(featuredItems, context, true)
        }

        if (IsServerOffline.value == true) {
            AlertDialog(
                onDismissRequest = { IsServerOffline.value = false },
                title = {
                    Text(
                        "FlixY Server ~",
                        fontFamily = sans_bold
                    )
                },
                text = {
                    Text(
                        "The server is currently offline. Please try again later.",
                        fontFamily = sans_bold
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            IsServerOffline.value = false
                            gatherFeaturedItems(featuredItems, context, false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFF4B1606)
                        )
                    ) {
                        Text(
                            "Refresh",
                            fontFamily = sans_bold
                        )
                    }
                },
                containerColor = Color.Black,
                titleContentColor = Color.Red,
                textContentColor = Color.White
            )
        }

        val gradientColors = GetRandGradient()

        val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0.0f)
        val fling =
            PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1),
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = 0.2f,
                )
            )


        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(gradientColors.first()),
                            Color(gradientColors.last())
                        ),
                        startY = 0.0f,
                        endY = 700.0f,
                        tileMode = TileMode.Clamp
                    ),
                    shape = RoundedCornerShape(0.dp),
                    alpha = 0.9f
                )
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    top = innerPadding.calculateTopPadding(),
                )
        ) {
            Column(
                modifier =
                Modifier
                    .padding(6.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    HorizontalPager(
                        pageCount = 15,
                        state = pagerState,
                        flingBehavior = fling,
                        beyondBoundsPageCount = 2,
                    ) { page ->
                        val showShimmer = remember { mutableStateOf(true) }
                        val showSpinner = remember { mutableStateOf(false) }
                        showSpinner.value =
                            (featuredItems.value.isEmpty() || featuredItems.value.size < 15)

                        if (!showSpinner.value) {
                            Card(
                                Modifier
                                    .height(
                                        520.dp
                                    )
                                    .width(
                                        configuration.screenWidthDp.dp.times(0.95f)
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                    )
                                    .graphicsLayer {
                                        val pageOffset =
                                            ((pagerState.currentPage - page) +
                                                    pagerState.currentPageOffsetFraction)
                                                .absoluteValue
                                        alpha =
                                            lerp(
                                                start = 0.0f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                    }
                                    .shadow(
                                        elevation = 10.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = Color.White,
                                        //clip = true,
                                        spotColor = Color.White
                                    )
                                    .drawWithContent { // Add a border
                                        drawContent()
                                        drawRoundRect(
                                            color = Color.Transparent,
                                            cornerRadius = CornerRadius(12.dp.toPx()),
                                            style = Stroke(width = 1.dp.toPx()),
                                            colorFilter = ColorFilter.lighting(
                                                add = Color(0xFF4B1606),
                                                multiply = Color.Red
                                            )
                                        )
                                    }
                                    .border(
                                        1.dp.times(0.2f),
                                        Color.Gray,
                                        RoundedCornerShape(12.dp)
                                    )


                            ) {
                                Box(
                                    modifier = Modifier.padding(0.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val quality =
                                        featuredItems.value.getOrNull(page)?.quality ?: "N/A"
                                    if (quality != "Unknown") {
                                        Row(
                                            modifier = Modifier
                                                .zIndex(6f)
                                                .padding(
                                                    top = 0.dp,
                                                    bottom = 478.dp,
                                                    end = 240.dp,
                                                )
                                                .width(
                                                    100.dp
                                                ),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Button(
                                                onClick = { /*TODO*/ },
                                                modifier = Modifier
                                                    .background(
                                                        Color.Transparent
                                                    )
                                                    .clip(
                                                        RoundedCornerShape(
                                                            5.dp
                                                        )
                                                    )
                                                    .padding(
                                                        top = 0.dp,
                                                        start = 0.dp,
                                                    )
                                                    .width(
                                                        30.dp
                                                    )
                                                    .height(
                                                        30.dp
                                                    ),
                                                contentPadding = PaddingValues(0.dp),

                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color.Black
                                                ),
                                                border = BorderStroke(
                                                    width = 0.dp,
                                                    color = Color.White
                                                ),
                                            ) {
                                                Text(
                                                    text = quality,
                                                    fontSize = 10.sp,
                                                    fontFamily = sans_bold,
                                                    modifier = Modifier
                                                        .zIndex(2f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .zIndex(6f)
                                            .padding(
                                                top = 220.dp,
                                                bottom = 0.dp
                                            )
                                            .width(
                                                280.dp
                                            ),
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { /*TODO*/ },
                                            modifier = Modifier
                                                .background(
                                                    Color.Transparent
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        5.dp
                                                    )
                                                )
                                                .padding(
                                                    top = 230.dp,
                                                    start = 0.dp,
                                                )
                                                .width(
                                                    135.dp
                                                )
                                                .height(
                                                    35.dp
                                                )
                                                .shadow(
                                                    elevation = 12.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    ambientColor = Color.White,
                                                    clip = true,
                                                    spotColor = Color.White
                                                ),
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = Color.Black
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color.White
                                            ),
                                        ) {
                                            Image(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clip(
                                                        RoundedCornerShape(
                                                            5.dp
                                                        )
                                                    ),
                                                contentScale = ContentScale.Crop,
                                                colorFilter =
                                                ColorFilter.lighting(
                                                    add = Color(0xFF4B1606),
                                                    multiply = Color.Black
                                                )
                                            )
                                            Text(
                                                text = "Play",
                                                fontSize = 10.sp,
                                                fontFamily = sans_bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = { /*TODO*/ },
                                            modifier = Modifier
                                                .background(
                                                    Color.Transparent
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        5.dp
                                                    )

                                                )
                                                .padding(
                                                    top = 140.dp,
                                                    end = 0.dp,
                                                )
                                                .width(
                                                    150.dp
                                                )
                                                .height(
                                                    35.dp
                                                )
                                                .shadow(
                                                    elevation = 12.dp,
                                                    shape = RoundedCornerShape(2.dp),
                                                    ambientColor = Color.White,
                                                    clip = true,
                                                    spotColor = Color.White
                                                )
                                                .graphicsLayer { },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Black.copy(alpha = 0.5f),
                                                contentColor = Color.Black
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = Color.White
                                            ),
                                        ) {
                                            ColorFilter.lighting(
                                                add = Color(0xFF4B1606),
                                                multiply = Color.Black
                                            )
                                            Image(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clip(
                                                        RoundedCornerShape(
                                                            6.dp
                                                        )
                                                    ),
                                                contentScale = ContentScale.Crop,
                                                colorFilter =
                                                ColorFilter.lighting(
                                                    add = Color(0xFFFFFFFF),
                                                    multiply = Color.Black
                                                )
                                            )
                                            Text(
                                                text = "My List",
                                                fontSize = 10.sp,
                                                fontFamily = sans_bold,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .zIndex(2f)
                                            )
                                        }
                                    }
                                    AsyncImage(
                                        model =
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(
                                                featuredItems.value.getOrNull(
                                                    page
                                                )?.poster?.replace("184x275", "500x750")
                                                    ?: ""
                                            )
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Title Poster",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                shimmerBrush(
                                                    targetValue = 1300f,
                                                    showShimmer = showShimmer.value
                                                )
                                            )
                                            .background(
                                                Color.Transparent
                                            )
                                            .clickable {
                                                clickedName =
                                                    featuredItems.value.getOrNull(page)?.title
                                                        ?: "-"
                                                clickedID =
                                                    featuredItems.value.getOrNull(page)?.href
                                                        ?: "tt4154796"
                                                mediaType =
                                                    featuredItems.value.getOrNull(page)?.category
                                                        ?: "movie"
                                                prevPageHistory.add(
                                                    PrevNav(
                                                        prevPage = "homePage",
                                                        prevID = clickedID,
                                                        prevName = clickedName,
                                                        prevMediaType = mediaType,
                                                        prevSearch = searchValue.value,
                                                    )
                                                )
                                                navController.navigate("videoScreen")
                                            },
                                        onSuccess = {
                                            showShimmer.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(4000)
                        if (featuredItems.value.isEmpty()) continue
                        val nextPage = pagerState.currentPage + 1
                        if (nextPage >= 15) {
                            pagerState.animateScrollToPage(0) // Go back to the first page
                        } else {
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CWBar(navController)
                }
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    TrendingBar(featuredItems.value, navController)

                    // TODO: nested LazyRow and LazyColumn
                }


                Spacer(modifier = Modifier.height(12.dp))
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TopCategoryHome() {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categories") }
    val categories = null

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(290.dp)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Box(
            modifier =
            Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = "TV Shows",
                fontSize = 14.sp,
                fontFamily = sans_bold,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Box(
            modifier =
            Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = "Movies",
                fontFamily = sans_bold,
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Box(
            modifier =
            Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = selectedCategory,
                fontSize = 14.sp,
                fontFamily = sans_bold,
                color = Color.LightGray,
                modifier =
                Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
            //DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            //  categories.forEach { category ->
            //    DropdownMenuItem(
            //      text = { Text(text = category) },
            //    onClick = {
            ///      selectedCategory = category
            //   expanded = false
            // },
            //c/olors = MenuDefaults.itemColors(
// color Set
            //)
            //)
            // }
            // }
        }
    }
}

data class Poster(var url: String, val height: Int, val width: Int)

data class Title(
    val title: String,
    val id: String,
    val poster: Poster,
    val year: Int,
    val type: String
)

data class GenreTitle(
    val title: String,
    val id: String,
    @com.google.gson.annotations.SerializedName("poster_url") val poster: String,
    val year: String,
)

data class Genre(
    val genre: String,
    @com.google.gson.annotations.SerializedName("result") val results: List<GenreTitle>,
)

fun fetchMovies(movies: MutableState<List<TmdbTitleMin>>) {
    println("CALLING FETCH MOVIES")
    val client = OkHttpClient()
    val randOne = (1..5).random()
    var randTwo = (1..5).random()
    var randThree = (6..10).random()
    while (randOne == randTwo) {
        randTwo = (1..5).random()
    }
    val request =
        Request.Builder()
            .url("https://api.themoviedb.org/3/trending/all/day?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=${randOne}")
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle network request failure
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    val moviesVar = Gson().fromJson(json, TmdbSimilar::class.java)
                    val request = Request.Builder()
                        .url("https://api.themoviedb.org/3/trending/all/day?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=${randTwo}")
                        .build()

                    val respTwo = client.newCall(request)
                        .execute().body?.string()
                    val moviesVarTwo = Gson().fromJson(respTwo, TmdbSimilar::class.java)
                    val requestThree = Request.Builder()
                        .url("https://api.themoviedb.org/3/trending/all/day?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=${randThree}")
                        .build()

                    val respThree = client.newCall(requestThree)
                        .execute().body?.string()
                    val moviesVarThree = Gson().fromJson(respThree, TmdbSimilar::class.java)
                    movies.value = moviesVar.results + moviesVarTwo.results + moviesVarThree.results
                }
            }
        )
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TrendingBar(
    items: List<featuredItem>,
    navController: NavHostController
) {
    if (items.isNotEmpty()) {
        Column {
            Row {
                Text(
                    text = "Trending Now",
                    fontSize = 16.sp,
                    fontFamily = sans_bold,
                    color = Color.White
                )
            }
            var shuffledItems = items.drop(15)

            val lazyListState = rememberLazyListState()

            Row {
                var pageSize = shuffledItems.size / 3
                if (pageSize == 0) {
                    pageSize = 1
                }
                LazyRow(
                    modifier =
                    Modifier,
                    //.clip(RoundedCornerShape(1.dp)),
                    state = lazyListState,
                ) {
                    itemsIndexed(shuffledItems) { index, item ->
                        val showShimmer = remember { mutableStateOf(true) }
                        val pageIndex = index / pageSize
                        val pageStartIndex = pageIndex * pageSize
                        val pageEndIndex = (pageIndex + 1) * pageSize - 1

                        if (index in pageStartIndex..pageEndIndex) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(168.dp)
                                    .padding(4.dp)
                                // .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(item.poster)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Movie Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            shimmerBrush(
                                                targetValue = 1300f,
                                                showShimmer = showShimmer.value
                                            )
                                        )
                                        .clickable {
                                            clickedName = item.title
                                            clickedID = item.href
                                            mediaType = item.category
                                            prevPageHistory.add(
                                                PrevNav(
                                                    prevPage = "homePage",
                                                    prevID = clickedID,
                                                    prevName = clickedName,
                                                    prevMediaType = mediaType,
                                                    prevSearch = "",
                                                )
                                            )
                                            navController.navigate("videoScreen")
                                        },
                                    onSuccess = {
                                        showShimmer.value = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }


            //  randomGenres = randomGenres.toList().shuffled().take(10).toTypedArray()
            //val data = remember { mutableStateOf<List<Genre>>(emptyList()) }
            //LaunchedEffect(Unit) {
            //  fetchGenres(randomGenres, data)
            //}
            //ContinueWatchingBar(nav = navController)
            //if (!data.value.isNullOrEmpty() && !data.value[0].results.isNullOrEmpty()) {
            //  //LazyColumn() {
            //l    for (genre in data.value) {
            //     if (!genre.genre.isNullOrEmpty() && !genre.results.isNullOrEmpty())
            //   TitleGenreBar(genre.genre, navController, genre.results)
            //}
            //} else {
            //  println("EMPTY GENRE")
            //}
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CWBar(
    navController: NavHostController
) {
    if (!CWAlreadyRead.value) {
        println("retrieving continue watching from internal storage")
        retrieveContinueWatchingFromInternalStorage(LocalContext.current)
        CWAlreadyRead.value = true
    }

    //deleteContinueWatchingFromInternalStorage(LocalContext.current)


    val items = ContinueWatching.value
    if (items.isNotEmpty()) {
        Column {
            Row {
                Text(
                    text = "Continue Watching",
                    fontSize = 16.sp,
                    fontFamily = sans_bold,
                    color = Color.White
                )
            }

            val lazyListState = rememberLazyListState()

            Row {
                var pageSize = items.size / 3
                if (pageSize == 0) {
                    pageSize = 1
                }
                LazyRow(
                    modifier =
                    Modifier,
                    //.clip(RoundedCornerShape(1.dp)),
                    state = lazyListState,
                ) {
                    itemsIndexed(items) { index, item ->
                        val showShimmer = remember { mutableStateOf(true) }
                        val pageIndex = index / pageSize
                        val pageStartIndex = pageIndex * pageSize
                        val pageEndIndex = (pageIndex + 1) * pageSize - 1

                        if (index in pageStartIndex..pageEndIndex) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(168.dp)
                                    .padding(4.dp)
                                // .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(item.poster)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Movie Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            shimmerBrush(
                                                targetValue = 1300f,
                                                showShimmer = showShimmer.value
                                            )
                                        )
                                        .clickable {
                                            clickedName = item.title
                                            clickedID = item.href
                                            mediaType = item.mediaType
                                            prevPageHistory.add(
                                                PrevNav(
                                                    prevPage = "homePage",
                                                    prevID = clickedID,
                                                    prevName = clickedName,
                                                    prevMediaType = mediaType,
                                                    prevSearch = "",
                                                )
                                            )
                                            navController.navigate("videoScreen")
                                        },
                                    onSuccess = {
                                        showShimmer.value = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }


            //  randomGenres = randomGenres.toList().shuffled().take(10).toTypedArray()
            //val data = remember { mutableStateOf<List<Genre>>(emptyList()) }
            //LaunchedEffect(Unit) {
            //  fetchGenres(randomGenres, data)
            //}
            //ContinueWatchingBar(nav = navController)
            //if (!data.value.isNullOrEmpty() && !data.value[0].results.isNullOrEmpty()) {
            //  //LazyColumn() {
            //l    for (genre in data.value) {
            //     if (!genre.genre.isNullOrEmpty() && !genre.results.isNullOrEmpty())
            //   TitleGenreBar(genre.genre, navController, genre.results)
            //}
            //} else {
            //  println("EMPTY GENRE")
            //}
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ContinueWatchingBar(nav: NavHostController) {
    if (!Auth.user.watched.isNullOrEmpty()) {
        Row {
            Text(
                text = "Continue Watching",
                fontSize = 16.sp,
                fontFamily = sans_bold,
                color = Color.White,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        var watched = mutableListOf<Watched>()
        for (watch in Auth.user.watched) {
            if (watched.find { it.name == watch.name } == null) {
                watched.add(watch)
            }
        }

        watched.reverse()

        Row {
            val lazyListState = rememberLazyListState()
            var pageSize = Auth.user.watched.size / 3
            if (pageSize == 0) {
                pageSize = 1
            }

            LazyRow(
                state = lazyListState,
            ) {
                itemsIndexed(watched) { index, item ->
                    val showShimmer = remember { mutableStateOf(true) }
                    val pageIndex = index / pageSize
                    val pageStartIndex = pageIndex * pageSize
                    val pageEndIndex = (pageIndex + 1) * pageSize - 1

                    if (index in pageStartIndex..pageEndIndex) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(168.dp)
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model =
                                ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        item.poster
                                    )
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Movie Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    )
                                    .clickable {
                                        clickedName = item.name
                                        if (item.tmdbID.isNotEmpty()) {
                                            clickedID = item.tmdbID
                                        } else {
                                            clickedID = item.imdbID
                                        }
                                        mediaType = item.type
                                        prevPageHistory.add(
                                            PrevNav(
                                                prevPage = "homePage",
                                                prevID = clickedID,
                                                prevName = clickedName,
                                                prevMediaType = mediaType,
                                                prevSearch = "",
                                            )
                                        )
                                        nav.navigate("videoScreen")
                                    },
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


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TitleGenreBar(
    genre: String,
    navController: NavHostController,
    data: List<GenreTitle>
) {
    if (!data.isNullOrEmpty()) {
        Row {
            Text(
                text = "Top $genre",
                fontSize = 16.sp,
                fontFamily = sans_bold,
                color = Color.White,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        Row {
            val lazyListState = rememberLazyListState()
            var pageSize = data.size / 3
            if (pageSize == 0) {
                pageSize = 1
            }


            LazyRow(
                modifier =
                Modifier,
                //.clip(RoundedCornerShape(12.dp)),
                state = lazyListState,
            ) {
                itemsIndexed(data) { index, item ->
                    val showShimmer = remember { mutableStateOf(true) }
                    val pageIndex = index / pageSize
                    val pageStartIndex = pageIndex * pageSize
                    val pageEndIndex = (pageIndex + 1) * pageSize - 1

                    if (index in pageStartIndex..pageEndIndex) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(168.dp)
                                .padding(4.dp)
                            // .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model =
                                ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        (item.poster)
                                            ?: ""
                                    )
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Movie Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    )
                                    .clickable {
                                        clickedName = item.title
                                        clickedID = item.id
                                        mediaType = "movie"
                                        prevPageHistory.add(
                                            PrevNav(
                                                prevPage = "homePage",
                                                prevID = clickedID,
                                                prevName = clickedName,
                                                prevMediaType = mediaType,
                                                prevSearch = "",
                                            )
                                        )
                                        navController.navigate("videoScreen")
                                    },
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

fun fetchGenres(genres: Array<String>, data: MutableState<List<Genre>>): Int {
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url("https://a.ztorr.me/api/imdb?genres=Action,Adventure,Comedy&limit=20").build()
    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle network request failure
                }

                override fun onResponse(call: Call, response: Response) {
                    //val json = response.body?.string()
                    //val movies = Gson().fromJson(json, Array<Genre>::class.java).toList()
                    //data.value = movies.toMutableList()
                }
            }
        )
    return 3
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BottomBar(navController: NavHostController) {
    BottomAppBar(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(
                width = 30.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 1.dp,
                vertical = 0.dp
            ),
        containerColor = Color(0xFF161615),
        content = {
            Row(
                modifier =
                Modifier
                    .height(52.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier =
                    Modifier
                        .background(Color(0xFF161615))
                        .padding(
                            vertical = 1.dp,
                        ),
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
                                //
                                .padding(horizontal = 8.dp)
                                .padding(
                                    top = 1.dp,
                                )
                                .clickable { navController.navigate("homePage") },
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
                                    bottom = 1.dp,
                                )
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            vertical = 1.dp,
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier

                            .background(Color(0xFF161615)),
                    ) {
                        Image(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier
                                //
                                .padding(horizontal = 8.dp)
                                .padding(
                                    top = 1.dp,
                                )
                                .clickable { navController.navigate("searchPanel") },
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
                                    bottom = 1.dp,
                                )
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            vertical = 1.dp,
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xFF161615)),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.downloads),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(
                                    top = 3.dp + 1.dp.times(0.6f)
                                )
                                .width(22.dp)
                                .height(21.dp)
                            //.clickable { navController.navigate("searchPanel") },
                            , contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color(0xFFFFFFFF),
                                multiply = Color.White
                            )
                        )
                        Text(
                            text = "Downloads",
                            fontSize = 8.sp,
                            fontFamily = sans_bold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(
                                    bottom = 1.dp,
                                )
                        )
                    }

                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            vertical = 1.dp,
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier

                            .background(Color(0xFF161615)),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.tv),
                            contentDescription = null,
                            modifier = Modifier
                                //
                                .padding(horizontal = 8.dp)
                                .padding(
                                    top = 1.dp,
                                )
                                .width(24.dp)
                                .height(24.dp)
                                .clickable {
                                    IsLiveTVHome.value = true
                                    navController.navigate("liveTV")
                                },
                            contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color(0xFFFFFFFF),
                                multiply = Color.White
                            )
                        )
                        Text(
                            text = "Live TV",
                            fontSize = 8.sp,
                            fontFamily = sans_bold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(
                                    bottom = 1.dp,
                                )
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            vertical = 1.dp,
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xFF161615)),
                    ) {
                        Image(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(
                                    top = 1.dp,
                                )
                            //.clickable { navController.navigate("searchPanel") },
                            , contentScale = ContentScale.Crop,
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
                                    bottom = 1.dp,
                                )
                        )
                    }

                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp, vertical = 15.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter =
                    ColorFilter.lighting(
                        add = Color(0xFF4B1606),
                        multiply = Color.Red
                    )
                )

                Image(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp, vertical = 15.dp)
                        .clickable {
                            prevPageHistory.add(
                                PrevNav(
                                    prevPage = "homePage",
                                    prevID = clickedID,
                                    prevName = clickedName,
                                    prevMediaType = mediaType,
                                    prevSearch = searchValue.value,
                                )
                            )
                            navController.navigate("searchPanel")
                        },
                    contentScale = ContentScale.Crop,
                    colorFilter =
                    ColorFilter.lighting(
                        add = Color(0xFFFFFFFF),
                        multiply = Color.White
                    )
                )
            }
        },
        colors =
        TopAppBarDefaults.smallTopAppBarColors(
            titleContentColor = Color.Transparent,
            containerColor = Color.Transparent.copy(alpha = 0.1f),
        )
    )
}