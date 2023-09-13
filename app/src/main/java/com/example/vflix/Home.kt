package com.example.vflix

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

data class PrevNav (
    var prevPage: String,
    var prevID: String,
    var prevName: String,
    var prevMediaType: String,
    var prevSearch: String,
    var se: Pair<Int, Int> = Pair(1, 1),
)

var prevPageHistory = mutableListOf<PrevNav>()

var randomGenres = arrayOf(
    "Action",
    "Adventure",
    "Animation",
    "Biography",
    "Comedy",
    "Crime",
    "Documentary",
    "Drama",
    "Family",
    "Fantasy",
    "Film-Noir",
    "Game-Show",
    "History",
    "Horror",
    "Music",
    "Musical",
    "Mystery",
    "News",
    "Reality-TV",
    "Romance",
    "Sci-Fi",
    "Sport",
    "Talk-Show",
    "Thriller",
    "War",
    "Western"
)


var clickedName = ""
var clickedID =  ""
var mediaType =""
var titles = mutableStateOf(emptyList<TmdbTitleMin>())

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
fun HomePage() {
    var navController = rememberNavController()
    LaunchedEffect(Unit) {
        if (titles.value.isNullOrEmpty()) {
            fetchMovies(titles)
        }
    }


    NavHost(navController, startDestination = "homePage") {
        composable(route = "homePage") {
            val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0.0f)
            val fling =
                PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(2)
                )
            var featuredItems = emptyList<TmdbTitleMin>()
            if (!titles.value.isNullOrEmpty()) {
                featuredItems = titles.value.take(15).shuffled()
            }
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0D0D))
            ) {
                Column(
                    modifier =
                    Modifier
                        .padding(6.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
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
                                    androidx.compose.ui.graphics.ColorFilter.lighting(
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
                                            navController.navigate("searchPanel") },
                                    contentScale = ContentScale.Crop,
                                    colorFilter =
                                    androidx.compose.ui.graphics.ColorFilter.lighting(
                                        add = Color(0xFFFFFFFF),
                                        multiply = Color.White
                                    )
                                )
                            }
                        },
                        colors =
                        TopAppBarDefaults.smallTopAppBarColors(
                            titleContentColor = Color.Transparent,
                            containerColor = Color.Transparent,
                        )
                    )

                    Row(modifier = Modifier.padding(9.dp)) { TopCategoryHome() }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        HorizontalPager(
                            pageCount = 15,
                            state = pagerState,
                            flingBehavior = fling,
                            beyondBoundsPageCount = 2,
                        ) { page ->
                            val showShimmer = remember { mutableStateOf(true) }
                            Card(
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 20.dp,
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
                            ) {
                                AsyncImage(
                                    model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(
                                            "https://image.tmdb.org/t/p/w1280" + featuredItems.getOrNull(
                                                page
                                            )?.poster
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
                                                featuredItems.getOrNull(page)?.title ?: featuredItems.getOrNull(
                                                    page
                                                )?.name ?: "-"
                                            clickedID =
                                                featuredItems.getOrNull(page)?.id ?: "tt4154796"
                                            mediaType = featuredItems.getOrNull(page)?.media_type
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
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(4000)
                            if (featuredItems.isEmpty()) continue
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
                        TrendingBar(titles.value, navController)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BottomAppBar(
                        //elevation = FloatingActionButtonDefaults.elevation(
                            //defaultElevation = 4.dp,
                        //),
                        containerColor = Color.Transparent,
                        //onClick = { navController.navigate("searchPanel") },
                        content = {
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
                                        modifier = Modifier

                                            .background(Color.Black)
                                    ) {
                                        Image(
                                            imageVector = Icons.Filled.Home,
                                            contentDescription = null,
                                            modifier = Modifier
                                                //
                                                .padding(horizontal = 8.dp)
                                                .padding(
                                                    top = 8.dp,
                                                )
                                                .clickable { navController.navigate("homePage") },
                                            contentScale = ContentScale.Crop,
                                            colorFilter =
                                            androidx.compose.ui.graphics.ColorFilter.lighting(
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
                                        modifier = Modifier

                                            .background(Color.Black)
                                    ) {
                                        Image(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            modifier = Modifier
                                                //
                                                .padding(horizontal = 8.dp)
                                                .padding(
                                                    top = 8.dp,
                                                )
                                                .clickable { navController.navigate("searchPanel") },
                                            contentScale = ContentScale.Crop,
                                            colorFilter =
                                            androidx.compose.ui.graphics.ColorFilter.lighting(
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
                                        modifier = Modifier

                                            .background(Color.Black)
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
                                                //.clickable { navController.navigate("searchPanel") },
                                            ,contentScale = ContentScale.Crop,
                                            colorFilter =
                                            androidx.compose.ui.graphics.ColorFilter.lighting(
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
                    )
                }
            }
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
    }
}

@Composable
fun TopCategoryHome() {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categories") }
    val categories = randomGenres

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
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(text = category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
// color Set
                        )
                    )
                }
            }
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
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url("https://api.themoviedb.org/3/trending/all/day?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=${(1..5).random()}")
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
                    movies.value = moviesVar.results
                }
            }
        )
}

@Composable
fun TrendingBar(
    _items: List<TmdbTitleMin>,
    navController: NavHostController
) {
    Column {
        Row {
            Text(
                text = "Trending Now",
                fontSize = 18.sp,
                fontFamily = sans_bold,
                color = Color.White
            )
        }
        var shuffledItems = emptyList<TmdbTitleMin>()
        if (!_items.isNullOrEmpty()) {
            val items = _items.drop(15)
            shuffledItems = items.shuffled()
        }

        val lazyListState = rememberLazyListState()

        Row {
            var pageSize = shuffledItems.size / 3
            if (pageSize == 0) {
                pageSize = 1
            }
            LazyRow(
                modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp)),
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
                                .height(180.dp)
                                .padding(4.dp)
                            // .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model =
                                ImageRequest.Builder(LocalContext.current)
                                    .data("https://image.tmdb.org/t/p/w342" + item.poster)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Movie Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    )
                                    .clickable {
                                        if (!item.title.isNullOrEmpty()) {
                                            clickedName = item.title
                                        } else {
                                            clickedName = item.name
                                        }
                                        clickedID = item.id
                                        mediaType = item.media_type
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


        randomGenres = randomGenres.toList().shuffled().take(6).toTypedArray()
        val data = remember { mutableStateOf<List<Genre>>(emptyList()) }
        LaunchedEffect(Unit) {
            fetchGenres(randomGenres, data)
        }
        if (!data.value.isNullOrEmpty() && !data.value[0].results.isNullOrEmpty()) {
            for (genre in data.value) {
                TitleGenreBar(genre.genre, navController, genre.results)
            }
        }
    }
}


@Composable
fun TitleGenreBar(
    genre: String,
    navController: NavHostController,
    data: List<GenreTitle>
) {
    Row {
        Text(
            text = "Top $genre Titles",
            fontSize = 18.sp,
            fontFamily = sans_bold,
            color = Color.White,
            modifier = Modifier.padding(top = 10.dp)
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
            Modifier
                .clip(RoundedCornerShape(12.dp)),
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
                            .height(180.dp)
                            .padding(4.dp)
                        // .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model =
                            ImageRequest.Builder(LocalContext.current)
                                .data(
                                    item.poster.split(".jpg")[0] + "QL75_UX380_CR0,0,380,562.jpg"
                                        ?: ""
                                )
                                .crossfade(true)
                                .build(),
                            contentDescription = "Movie Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
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

fun fetchGenres(genres: Array<String>, data: MutableState<List<Genre>>): Int {
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url("https://a.ztorr.me/api/imdb?genres=${genres.joinToString(",")}&limit=20").build()
    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle network request failure
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    val movies = Gson().fromJson(json, Array<Genre>::class.java).toList()
                    data.value = movies.toMutableList()
                }
            }
        )
    return 3
}

