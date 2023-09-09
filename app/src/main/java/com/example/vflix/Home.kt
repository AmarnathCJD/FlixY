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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.ui.theme.sans_bold
import com.google.gson.Gson
import java.io.IOException
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

var TEST_IMAGE_URLS =
    arrayOf(
        "https://m.media-amazon.com/images/M/MV5BNDJjMzc4NGYtZmFmNS00YWY3LThjMzQtYzJlNGFkZGRiOWI1XkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_QL75_UX380_CR0,0,380,562_.jpg",
        "https://m.media-amazon.com/images/M/MV5BNjJkYTI1MjAtNTcxZC00YmU5LWExMDAtZTg3YzRhMDNmYmEwXkEyXkFqcGdeQXVyMTEyMjM2NDc2._V1_FMjpg_UX604_.jpg",
        "https://m.media-amazon.com/images/M/MV5BODI0ZTljYTMtODQ1NC00NmI0LTk1YWUtN2FlNDM1MDExMDlhXkEyXkFqcGdeQXVyMTM0NTUzNDIy._V1_.jpg",
        "https://m.media-amazon.com/images/M/MV5BYTRmMWQxZGEtZTZiMS00ZTRiLWIyYmMtMzNmMmRjZjYyNGUyXkEyXkFqcGdeQXVyMTUzOTcyODA5._V1_.jpg",
    )

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
fun HomePage(titles: List<Title>) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "homePage") {
        composable(route = "homePage") {
            val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0.0f)
            val fling =
                PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(2)
                )
            val featuredItems = titles.take(5)
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painter =
                        painterResource(
                            R.drawable
                                .photo_background_green_textured_wall_rolling_floor_studio_photography_background_illuminated
                        ),
                        contentScale = ContentScale.Crop,
                    )
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
                                    contentDescription = "Mm",
                                    modifier = Modifier
                                        .height(52.dp)
                                        .padding(horizontal = 8.dp, vertical = 15.dp)
                                        .clickable {  navController.navigate("searchPannel") },
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
                            pageCount = 5,
                            state = pagerState,
                            flingBehavior = fling,
                            beyondBoundsPageCount = 2,
                        ) { page ->
                            val showShimmer = remember { mutableStateOf(true) }
                            Card(
                                Modifier
                                    .size(height = 380.dp, width = 280.dp)
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
                                            featuredItems.getOrNull(page)?.poster?.url
                                                ?: TEST_IMAGE_URLS[page]
                                        )
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Movie Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                        .background(
                                            shimmerBrush(
                                                targetValue = 1300f,
                                                showShimmer = showShimmer.value
                                            )
                                        ),
                                    error = painterResource(R.drawable.ic_launcher_background),
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
                            if (nextPage >= 5) {
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
                        TrendingBar(titles)
                        }
                    }
                }
            }
        composable(route = "searchPannel") {
            EnterAnimation {
                SearchPannel(navController)
            }
        }
    }
}

@Composable
fun TopCategoryHome() {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categories") }
    val categories = listOf("TO", "DO")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(230.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
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
                fontSize = 12.sp,
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
                fontSize = 12.sp,
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
                fontSize = 12.sp,
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
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TrendingBar(titles: List<Title>) {
    Column {
        Row {
            Text(
                text = "Trending Now",
                fontSize = 18.sp,
                fontFamily = sans_bold,
                color = Color.White
            )
        }
        TrendingPageBar(titles)
    }
}

data class Poster(val url: String, val height: Int, val width: Int)

data class Title(
    val title: String,
    val id: String,
    val poster: Poster,
    val year: Int,
    val type: String
)

class MovieViewModel : ViewModel() {
    private val _movies = MutableLiveData<List<Title>>()
    val movies: LiveData<List<Title>>
        get() = _movies

    fun fetchMovies() {
        val client = OkHttpClient()
        val request =
            Request.Builder().url("https://a.ztorr.me/api/imdb?trending=1&i=true&limit=25").build()

        client
            .newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Handle network request failure
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        val movies = Gson().fromJson(json, Array<Title>::class.java).toList()
                        _movies.postValue(movies)
                    }
                }
            )
    }
}

@Composable
fun TrendingPageBar(items: List<Title>) {
    val items = items.drop(5)
    val shuffledItems = remember(items) { items.shuffled() }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(remember { derivedStateOf { lazyListState.layoutInfo } }) {
        val layoutInfo = lazyListState.layoutInfo
        val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val preloadOffset = 6

        val startIndex = maxOf(0, firstVisibleItemIndex - preloadOffset)
        val endIndex =
            minOf(
                lazyListState.layoutInfo.totalItemsCount - 1,
                lastVisibleItemIndex + preloadOffset
            )

        // Preload data for items in the range [startIndex, endIndex]
        for (index in startIndex..endIndex) {
            // Preload data for item at index
            // Example: preloadData(index)
        }
    }

    Row {
        val pageSize = items.size / 3
        LazyRow(
            modifier =
            Modifier
                // .fillMaxWidth()
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
                                .data(item.poster?.url ?: TEST_IMAGE_URLS[0])
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
                                ),
                            error = painterResource(R.drawable.ic_launcher_background),
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
}
