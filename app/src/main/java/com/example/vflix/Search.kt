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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
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
val topDataBackup = mutableStateOf<List<NTSearch>>(emptyList())

val SearchBoxContent = mutableStateOf("")
val topTitlesShown = mutableStateOf(false)

var searchValue = mutableStateOf("")

// orientation 1 = portrait, 2 = landscape
val colFactor = mutableIntStateOf(3)

// errorMessageRelated
val showError = mutableStateOf(false)
val errorMessage = mutableStateOf("")

// loadingProgress
val showProgressSearch = mutableStateOf(false)
val showNoResultSearch = mutableStateOf(false)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPanel(nav: NavHostController) {
    val ctx = LocalContext.current
    val deviceHeight =
        ctx.resources.displayMetrics.heightPixels / ctx.resources.displayMetrics.density
    val deviceWidth =
        ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density

    val search = SearchData.value

    if (SearchBoxContent.value.isEmpty() && !showNoResultSearch.value) {
        if (topDataBackup.value.isEmpty()) {
            LaunchedEffect(Unit) {
                ImdbTop(
                    SearchData,
                    showProgressSearch,
                    showError,
                    errorMessage
                )
            }
        } else {
            SearchData.value = topDataBackup.value
        }
        topTitlesShown.value = true
    } else if (topTitlesShown.value) {
        topDataBackup.value = SearchData.value
        SearchData.value = emptyList()
        topTitlesShown.value = false
    }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Black)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            Image(
                painter = painterResource(id = R.drawable.cast_24),
                contentDescription = "Cast",
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 14.dp, vertical = 16.dp)
                    .clickable { },
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
                    horizontal = 2.dp,
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
                                showProgressSearch.value = true
                                INTSearchTitle(
                                    SearchBoxContent.value,
                                    SearchData,
                                    showProgressSearch,
                                    showNoResultSearch,
                                )
                            }
                        }.start()
                    }
                },
                placeholder = { Text("Search movies, shows, kdrama...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    placeholderColor = Color(0xFFC5C3C3),
                    unfocusedBorderColor = Color(0xFF1F1F1F),
                    focusedBorderColor = Color(0xFF1F1F1F),
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
                            .clickable { },
                        contentScale = ContentScale.Crop,
                        colorFilter =
                        ColorFilter.lighting(
                            add = Color(0xFFFFFFFF),
                            multiply = Color.White
                        )
                    )
                },
                trailingIcon = {
                    if (showProgressSearch.value) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp)
                                .padding(horizontal = 0.dp, vertical = 0.dp),
                            color = Color.Red
                        )
                    } else {
                        Image(
                            painter = if (SearchBoxContent.value.isEmpty()) painterResource(id = R.drawable.mic_24) else painterResource(
                                id = R.drawable.close_24
                            ),
                            contentDescription = "Close",
                            modifier = Modifier
                                .height(64.dp)
                                .padding(horizontal = 14.dp, vertical = 15.dp)
                                .clickable {
                                    if (SearchBoxContent.value.isNotEmpty()) {
                                        SearchBoxContent.value = ""
                                        SearchData.value = emptyList()
                                        showNoResultSearch.value = false
                                    }
                                },
                            contentScale = ContentScale.Crop,
                            colorFilter =
                            ColorFilter.lighting(
                                add = Color(0xFFFFFFFF),
                                multiply = Color.White
                            )
                        )
                    }
                }
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
            if (!showNoResultSearch.value) {
                Row(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = if (topTitlesShown.value) "Movies & TV" else "Search Results for \"${SearchBoxContent.value}\"",
                        modifier = Modifier
                            .padding(
                                vertical = 2.dp,
                                horizontal = 12.dp
                            )
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.White,
                            fontFamily = sans_bold,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                }
            }

            if (showNoResultSearch.value) {
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Oh darn. We don't have that.",
                        modifier = Modifier
                            .padding(
                                start = 36.dp,
                                top = 36.dp
                            )
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = Color.White,
                            fontFamily = sans_bold,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        textAlign = TextAlign.Start
                    )
                }

                Row(
                    modifier = Modifier

                        .fillMaxWidth()
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Try searching for another movie, show, or kdrama.",
                        modifier = Modifier
                            .padding(
                                start = 36.dp,
                            )
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = sans_bold
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }


            if (showError.value) {
                Row(
                    modifier = Modifier
                        .padding(top = deviceHeight.dp / 4)
                        .fillMaxWidth()
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage.value,
                        modifier = Modifier
                            .padding(
                                vertical = 2.dp,
                                horizontal = 12.dp
                            )
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontFamily = sans_bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 0.dp)
                        .fillMaxWidth()
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            showError.value = false
                            showProgressSearch.value = true

                            ImdbTop(
                                SearchData,
                                showProgressSearch,
                                showError,
                                errorMessage
                            )
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                        ),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Refresh")
                    }
                }
            }

            val orientation = ctx.resources.configuration.orientation

            if (orientation == 2) {
                colFactor.intValue = 6
            }

            val splitSearch = search.chunked(colFactor.intValue)
            val singleItemWidth = (deviceWidth / colFactor.intValue) - 12 // 12 is padding
            val aspectRatio = 1.61f // 16:10

            val singleItemHeight = singleItemWidth * aspectRatio
            for (i in splitSearch) {
                Spacer(
                    modifier = Modifier.height(
                        0.dp
                    )
                )

                val haveNoFill = colFactor.intValue - i.size

                var items = i
                if (haveNoFill > 0) {
                    for (j in 0 until haveNoFill) {
                        items = items + NTSearch("", "", "", "", "")
                    }
                }

                val lazyListState = rememberLazyListState()
                LazyRow(
                    modifier =
                    Modifier
                        .padding(
                            vertical = 2.dp,
                            horizontal = 6.dp
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    state = lazyListState,
                ) {
                    itemsIndexed(items) { index, item ->
                        val pageIndex = index / colFactor.intValue
                        val pageStartIndex = pageIndex * colFactor.intValue
                        val pageEndIndex = (pageIndex + 1) * colFactor.intValue - 1

                        if (index in pageStartIndex..pageEndIndex) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(1.dp)
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
                                                    3.dp
                                                )
                                            )
                                            .background(
                                                shimmerBrush(
                                                    targetValue = 600f,
                                                    showShimmer = if (item.poster.isEmpty()) false else showShimmer.value
                                                )
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
                                    1.dp
                                )
                            )
                        }
                        Spacer(
                            modifier = Modifier.height(
                                1.dp
                            )
                        )
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
