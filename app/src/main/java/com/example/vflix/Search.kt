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
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.vflix.api.INTSearchTitle
import com.example.vflix.api.ImdbTop
import com.example.vflix.api.NTSearch
import com.example.vflix.ui.theme.sans_bold

val SearchData = mutableStateOf<List<NTSearch>>(emptyList())
val SearchBoxContent = mutableStateOf("")
val topTitlesShown = mutableStateOf(false)

var searchValue = mutableStateOf("")

data class TrendingTitle(
    val id: String,
    val title: String?,
    @com.google.gson.annotations.SerializedName("original_name") val name: String?,
    @com.google.gson.annotations.SerializedName("backdrop_path") val poster: String?,
    @com.google.gson.annotations.SerializedName("poster_path") val poster2: String?,
    @com.google.gson.annotations.SerializedName("media_type") val mediaType: String?,
)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPanel(nav: NavHostController) {
    val showProgress = remember { mutableStateOf(true) }
    val showNoResult = remember { mutableStateOf(false) }


    val search = SearchData.value

    if (SearchBoxContent.value.isEmpty()) {
        Thread {
            ImdbTop(
                SearchData,
                showProgress,
            )
        }.start()

        topTitlesShown.value = true
    } else if (topTitlesShown.value) {
        SearchData.value = emptyList()
        topTitlesShown.value = false
    }

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
                    .padding(horizontal = 14.dp, vertical = 16.dp)
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
                value = SearchBoxContent.value,
                onValueChange = {
                    if (SearchBoxContent.value == it) {
                        SearchBoxContent.value = it
                    } else {
                        SearchBoxContent.value = it
                        Thread {
                            if (SearchBoxContent.value.length > 3) {
                                showProgress.value = true
                                INTSearchTitle(
                                    SearchBoxContent.value,
                                    SearchData,
                                    showProgress,
                                    showNoResult
                                )
                            }
                        }.start()
                    }
                },
                placeholder = { Text("Search for Movies, TV") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    placeholderColor = Color(0xFFC5C3C3),
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLeadingIconColor = Color(0xFF6200EE),
                ),
                textStyle = TextStyle(
                    fontSize = 19.sp,
                ),
                leadingIcon = {
                    Image(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 14.dp, vertical = 15.dp)
                            .clickable {  },
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
        Spacer(
            modifier = Modifier.height(
                4.dp
            )
        )

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            if (!showNoResult.value) {
                Row(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = if (topTitlesShown.value) "Top IMDB Titles" else "Search Results for \"${SearchBoxContent.value}\"",
                        modifier = Modifier
                            .padding(
                                vertical = 2.dp,
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

            val splitSearch = search.chunked(4)
            val ctx = LocalContext.current
            val deviceWidth = ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density
            val singleItemWidth = deviceWidth / 4
            val aspectRatio = 1.61f

            val singleItemHeight = singleItemWidth * aspectRatio

            for (i in splitSearch) {
                Spacer(
                    modifier = Modifier.height(
                        8.dp
                    )
                )
                val lazyListState = rememberLazyListState()
                LazyRow(
                    modifier =
                    Modifier
                        .padding(
                            vertical = 2.dp,
                            horizontal = 12.dp
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth(),
                    horizontalArrangement = if (i.size < 3) {
                        Arrangement.SpaceAround
                    } else {
                        Arrangement.Start
                    },
                    state = lazyListState,
                ) {
                    itemsIndexed(i) { index, item ->
                        val pageIndex = index / 4
                        val pageStartIndex = pageIndex * 4
                        val pageEndIndex = (pageIndex + 1) * 4 - 1

                        if (index in pageStartIndex..pageEndIndex) {
                            if ((item.poster.length ?: 0) > 0) {
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
                                                .data(item.poster)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Title Poster",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .height(singleItemHeight.dp)
                                                .width(singleItemWidth.dp)
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
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.DarkGray,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .clickable {
                                                    clickedName = item.title
                                                    clickedID = item.href
                                                    mediaType = item.category
                                                    prevPageHistory.add(
                                                        PrevNav(
                                                            prevPage = "searchPanel",
                                                            prevName = clickedName,
                                                            prevID = clickedID,
                                                            prevMediaType = mediaType,
                                                            prevSearch = SearchBoxContent.value,
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

                            Spacer(
                                modifier = Modifier.height(
                                    4.dp
                                )
                            )
                        }
                    }
                }
            }


            // if (actionOnly.isNotEmpty()) {
            //   setupGenre("Action", actionOnly, nav)
            //}
            //if (romanceOnly.isNotEmpty()) {
            //  setupGenre("Romance", romanceOnly, nav)
            //}
            //if (horrorOnly.isNotEmpty()) {
            //  setupGenre("Horror", horrorOnly, nav)
            //}
            //if (familyOnly.isNotEmpty()) {
            //  setupGenre("Family", familyOnly, nav)
            //}
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
