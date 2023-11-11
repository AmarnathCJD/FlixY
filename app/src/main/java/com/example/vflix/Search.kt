package com.example.vflix

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vflix.parser.Channel
import com.example.vflix.parser.SearchChannel
import com.example.vflix.ui.theme.sans_bold
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

var searchValue = mutableStateOf("")
val searchResult = mutableStateOf<List<TrendingTitle>>(emptyList())

data class SearchTitle(
    val id: String,
    val title: String,
    @com.google.gson.annotations.SerializedName("poster_url") val poster: String?,
)

data class TrendingTitle(
    val id: String,
    val title: String?,
    @com.google.gson.annotations.SerializedName("original_name") val name: String?,
    @com.google.gson.annotations.SerializedName("backdrop_path") val poster: String?,
    @com.google.gson.annotations.SerializedName("poster_path") val poster2: String?,
    @com.google.gson.annotations.SerializedName("media_type") val mediaType: String?,
)

data class TopResult(
    val results: List<TrendingTitle>
)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPanel(nav: NavHostController) {
    val trendingState = remember { mutableStateOf<List<TrendingTitle>>(emptyList()) }
    val actionState = remember { mutableStateOf<List<SearchTitle>>(emptyList()) }
    val romanceState = remember { mutableStateOf<List<SearchTitle>>(emptyList()) }
    val horrorState = remember { mutableStateOf<List<SearchTitle>>(emptyList()) }
    val familyState = remember { mutableStateOf<List<SearchTitle>>(emptyList()) }
    val channelState = remember { mutableStateOf<List<Channel>>(emptyList()) }
    val showProgress = remember { mutableStateOf(true) }
    val showNoResult = remember { mutableStateOf(false) }
    val displayTrending = remember { mutableStateOf(true) }

    displayTrending.value = searchValue.value.isNullOrEmpty()

    LaunchedEffect(Unit) {
        fetchTopSearchResult(trendingState, showProgress)
    }

    val search = searchResult.value
    val trending = trendingState.value

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Black)

    ) {
        Row {
            Image(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 12.dp, vertical = 15.dp)
                    .clickable { nav.navigate("homePage") },
                contentScale = ContentScale.Crop,
                colorFilter =
                ColorFilter.lighting(
                    add = Color(0xFFFFFFFF),
                    multiply = Color.White
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 2.dp,
                )
        ) {
            OutlinedTextField(
                value = searchValue.value,
                onValueChange = {
                    searchValue.value = it
                    if (searchValue.value.length % 2 == 0 && !searchValue.value.isNullOrEmpty()) {
                        displayTrending.value = false
                        showProgress.value = true
                        SearchChannel(searchValue.value, channelState)
                        fetchResult(searchResult, showProgress, searchValue.value, showNoResult)
                    }
                },
                placeholder = { Text("Search for titles, genres or people") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    containerColor = Color.DarkGray,
                    placeholderColor = Color.Gray,
                    focusedBorderColor = Color.Transparent,
                ),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                ),
                leadingIcon = {
                    Image(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Mm",
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 12.dp, vertical = 15.dp)
                            .clickable { nav.navigate("homePage") },
                        contentScale = ContentScale.Crop,
                        colorFilter =
                        ColorFilter.lighting(
                            add = Color(0xFFFFFFFF),
                            multiply = Color.White
                        )
                    )
                },
            )
        }
        if (displayTrending.value) {
            Row {
                Text(
                    text = "Top Searches",
                    modifier = Modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 12.dp
                        )
                        .fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.White,
                        fontFamily = sans_bold
                    )
                )
            }
            Row(
                modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp)),

                ) {

                val lazyListState = rememberLazyListState()

                var pageSize = trending.size / 3
                if (pageSize == 0) {
                    pageSize = 1
                }
                LazyColumn(
                    modifier =
                    Modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 12.dp
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth(),
                    state = lazyListState,
                ) {
                    itemsIndexed(trending) { index, item ->
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
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model =
                                            ImageRequest.Builder(LocalContext.current)//"https://image.tmdb.org/t/p/w1280"
                                                .data("https://image.tmdb.org/t/p/w500" + item.poster)//item.poster?.url ?: TEST_IMAGE_URLS[0])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Movie Poster",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .height(90.dp)
                                                .width(160.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .clickable {
                                                    clickedName = item.title
                                                        ?: (item.name ?: "")
                                                    clickedID = item.id
                                                    mediaType = item.mediaType ?: "movie"
                                                    prevPageHistory.add(
                                                        PrevNav(
                                                            prevPage = "searchPanel",
                                                            prevName = clickedName,
                                                            prevID = clickedID,
                                                            prevMediaType = mediaType,
                                                            prevSearch = searchValue.value,
                                                        )
                                                    )
                                                    nav.navigate("videoScreen")
                                                }
                                                .background(
                                                    shimmerBrush(
                                                        targetValue = 1300f,
                                                        showShimmer = showShimmer.value
                                                    )
                                                ),
                                        )
                                        Text(
                                            text = item.title
                                                ?: (item.name ?: ""),
                                            modifier = Modifier
                                                .padding(
                                                    vertical = 6.dp,
                                                    horizontal = 12.dp
                                                )
                                                .fillMaxWidth(),
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                color = Color.White,
                                                fontFamily = sans_bold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (!showNoResult.value) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Top Results",
                            modifier = Modifier
                                .padding(
                                    vertical = 2.dp,
                                    horizontal = 12.dp
                                )
                                .fillMaxWidth(),
                            style = TextStyle(
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = sans_bold
                            )
                        )
                    }
                }
                if (showNoResult.value) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Results",
                            modifier = Modifier
                                .padding(
                                    vertical = 10.dp,
                                    horizontal = 12.dp
                                )
                                .fillMaxWidth(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontFamily = sans_bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }


                Row {
                    val lazyListState = rememberLazyListState()

                    var pageSize = search.size / 3
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
                        itemsIndexed(search) { index, item ->
                            val pageIndex = index / pageSize
                            val pageStartIndex = pageIndex * pageSize
                            val pageEndIndex = (pageIndex + 1) * pageSize - 1

                            if (index in pageStartIndex..pageEndIndex) {
                                if ((item.poster2?.length ?: 0) > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(2.dp)
                                    ) {
                                        val showShimmer = remember { mutableStateOf(true) }
                                        Row {
                                            AsyncImage(
                                                model =
                                                ImageRequest.Builder(LocalContext.current)
                                                    .data("https://image.tmdb.org/t/p/w342" + item.poster2)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Movie Poster",
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
                                                        clickedName = item.title
                                                            ?: (item.name ?: "")
                                                        clickedID = item.id
                                                        mediaType = item.mediaType ?: "movie"
                                                        prevPageHistory.add(
                                                            PrevNav(
                                                                prevPage = "searchPanel",
                                                                prevName = clickedName,
                                                                prevID = clickedID,
                                                                prevMediaType = mediaType,
                                                                prevSearch = searchValue.value,
                                                            )
                                                        )
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

                if (!channelState.value.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Live TV",
                            modifier = Modifier
                                .padding(
                                    vertical = 2.dp,
                                    horizontal = 12.dp
                                )
                                .fillMaxWidth(),
                            style = TextStyle(
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = sans_bold
                            )
                        )
                    }
                    Row {
                        val lazyListState = rememberLazyListState()

                        var pageSize = channelState.value.size / 3
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
                            itemsIndexed(channelState.value) { index, item ->
                                val pageIndex = index / pageSize
                                val pageStartIndex = pageIndex * pageSize
                                val pageEndIndex = (pageIndex + 1) * pageSize - 1

                                if (index in pageStartIndex..pageEndIndex) {
                                    if ((item.image?.length ?: 0) > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(2.dp)
                                        ) {
                                            val showShimmer = remember { mutableStateOf(true) }
                                            Row {
                                                AsyncImage(
                                                    model =
                                                    ImageRequest.Builder(LocalContext.current)
                                                        .data(item.image)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Movie Poster",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .height(130.dp)
                                                        .width(130.dp)
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
                                                            clickedName = item.name
                                                            clickedID = item.id
                                                            mediaType = item.image ?: "live"
                                                            IsLiveTVHome.value = false
                                                            prevPageHistory.add(
                                                                PrevNav(
                                                                    prevPage = "searchPanel",
                                                                    prevName = clickedName,
                                                                    prevID = clickedID,
                                                                    prevMediaType = mediaType,
                                                                    prevSearch = searchValue.value,
                                                                )
                                                            )
                                                            nav.navigate("liveTV")
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

                LaunchedEffect(Unit) {
                    searchTitle(
                        actionState,
                        showProgress,
                        searchValue.value,
                        showNoResult,
                        "Action"
                    )
                    searchTitle(
                        romanceState,
                        showProgress,
                        searchValue.value,
                        showNoResult,
                        "Romance"
                    )
                    searchTitle(
                        horrorState,
                        showProgress,
                        searchValue.value,
                        showNoResult,
                        "Horror"
                    )
                    searchTitle(
                        familyState,
                        showProgress,
                        searchValue.value,
                        showNoResult,
                        "Family"
                    )
                }

                val actionOnly = actionState.value
                val romanceOnly = romanceState.value
                val horrorOnly = horrorState.value
                val familyOnly = familyState.value

                if (actionOnly.isNotEmpty()) {
                    setupGenre("Action", actionOnly, nav)
                }
                if (romanceOnly.isNotEmpty()) {
                    setupGenre("Romance", romanceOnly, nav)
                }
                if (horrorOnly.isNotEmpty()) {
                    setupGenre("Horror", horrorOnly, nav)
                }
                if (familyOnly.isNotEmpty()) {
                    setupGenre("Family", familyOnly, nav)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun setupGenre(genre: String, data: List<SearchTitle>, nav: NavHostController) {
    Row(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            text = "Top $genre",
            modifier = Modifier
                .padding(
                    vertical = 2.dp,
                    horizontal = 12.dp
                )
                .fillMaxWidth(),
            style = TextStyle(
                fontSize = 18.sp,
                color = Color.White,
                fontFamily = sans_bold
            )
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
                .padding(
                    vertical = 2.dp,
                    horizontal = 12.dp
                )
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth(),
            state = lazyListState,
        ) {
            itemsIndexed(data) { index, item ->
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
                                        .data(
                                            (item.poster?.split(".jpg")?.get(0)
                                                ?: "") + "QL75_UX380_CR0,0,380,562.jpg"
                                        )
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Movie Poster",
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
                                            clickedName = item.title
                                            clickedID = item.id
                                            prevPageHistory.add(
                                                PrevNav(
                                                    prevPage = "searchPanel",
                                                    prevName = clickedName,
                                                    prevID = clickedID,
                                                    prevMediaType = mediaType,
                                                    prevSearch = searchValue.value,
                                                )
                                            )
                                            nav.navigate("videoScreen")
                                        },
                                    onSuccess = {
                                        showShimmer.value = false
                                    },
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

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

fun fetchTopSearchResult(
    l: MutableState<List<TrendingTitle>>,
    showProgress: MutableState<Boolean>
) {
    // val url = "https://api.themoviedb.org/3/search/tv?api_key=d56e51fb77b081a9cb5192eaaa7823ad&query=Sex"
    //val url = "https://api.themoviedb.org/3/trending/all/day?api_key=d56e51fb77b081a9cb5192eaaa7823ad&page=$page"
    val pageNo = (1..10).random()
    val url =
        "https://api.themoviedb.org/3/discover/movie?api_key=d56e51fb77b081a9cb5192eaaa7823ad&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=$pageNo"
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
                    val movies = Gson().fromJson(json, TopResult::class.java)
                    l.value = movies.results
                    showProgress.value = false
                }
            }
        )
}

fun fetchResult(
    l: MutableState<List<TrendingTitle>>,
    showProgress: MutableState<Boolean>,
    q: String,
    noResult: MutableState<Boolean>
) {
    val url =
        "https://api.themoviedb.org/3/search/multi?api_key=d56e51fb77b081a9cb5192eaaa7823ad&query=${
            URLEncoder.encode(
                q,
                "UTF-8"
            )
        }"
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
                    val movies = Gson().fromJson(json, TopResult::class.java)
                    if (movies.results.isNullOrEmpty()) {
                        l.value = emptyList()
                        noResult.value = true
                    } else {
                        l.value = movies.results
                        noResult.value = false
                    }
                    println("TMDB: $movies")
                    showProgress.value = false
                }
            }
        )
}

fun searchTitle(
    l: MutableState<List<SearchTitle>>,
    showProgress: MutableState<Boolean>,
    q: String,
    noResult: MutableState<Boolean>,
    type: String
) {
    var url = "https://a.ztorr.me/api/imdb?q=${URLEncoder.encode(q, "UTF-8")}"
    var isAddon = false
    if (type != "all") {
        url = "https://a.ztorr.me/api/imdb?genre=$type"
        isAddon = true
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
                    val movies = Gson().fromJson(json, Array<SearchTitle>::class.java)
                    println("Query: $q")
                    if (movies.isNullOrEmpty()) {
                        l.value = emptyList()
                        if (!isAddon) {
                            noResult.value = true
                        }
                    } else {
                        if (isAddon) {
                            val shuffled = movies.toList()
                            l.value = shuffled
                        } else {
                            println("MoviesQ: $movies")
                            l.value = movies.toList()
                        }
                        if (!isAddon) {
                            noResult.value = false
                        }
                    }
                    showProgress.value = false
                }
            }
        )
}