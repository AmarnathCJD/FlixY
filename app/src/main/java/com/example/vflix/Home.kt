package com.example.vflix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.runtime.Composable
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.absoluteValue

@Composable
fun MyAppHome() {
    //LoginForm()
    HomePage()
}

var TEST_IMAGE_URLS = arrayOf(
    "https://m.media-amazon.com/images/M/MV5BNDJjMzc4NGYtZmFmNS00YWY3LThjMzQtYzJlNGFkZGRiOWI1XkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_QL75_UX380_CR0,0,380,562_.jpg",
    "https://m.media-amazon.com/images/M/MV5BNjJkYTI1MjAtNTcxZC00YmU5LWExMDAtZTg3YzRhMDNmYmEwXkEyXkFqcGdeQXVyMTEyMjM2NDc2._V1_FMjpg_UX604_.jpg",
    "https://m.media-amazon.com/images/M/MV5BODI0ZTljYTMtODQ1NC00NmI0LTk1YWUtN2FlNDM1MDExMDlhXkEyXkFqcGdeQXVyMTM0NTUzNDIy._V1_.jpg",
    "https://m.media-amazon.com/images/M/MV5BYTRmMWQxZGEtZTZiMS00ZTRiLWIyYmMtMzNmMmRjZjYyNGUyXkEyXkFqcGdeQXVyMTUzOTcyODA5._V1_.jpg",
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage() {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0.0f
    )
    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(10)
    )
Column (
    modifier = Modifier
        .fillMaxSize()
        .paint(
            painter = painterResource(R.drawable.photo_background_green_textured_wall_rolling_floor_studio_photography_background_illuminated),
            contentScale = ContentScale.Crop,
            //colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFAD2B02))
        )
){


    Column(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxSize(),
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            TopLogoHome()
        }
        Row(
            modifier = Modifier
                .padding(9.dp)
        ) {
            TopCategoryHome()
        }
        Row {
            HorizontalPager(
                pageCount = 4, state = pagerState, flingBehavior = fling
            ) { page ->
                Card(
                    Modifier
                        .size(
                            height = 520.dp,
                            width = 350.dp
                        )
                        .graphicsLayer {
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState
                                        .currentPageOffsetFraction
                                    ).absoluteValue
                            alpha = lerp(
                                start = 0.0f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(TEST_IMAGE_URLS[page])
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_launcher_background),
                        contentDescription = "Movie Poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                border = BorderStroke(1.dp, Color.LightGray),
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                    )
                }
            }
        }
    }

}
}

@Composable
fun TopLogoHome() {
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = null,
        modifier = Modifier
            .height(52.dp)
            .padding(
                horizontal = 8.dp,
                vertical = 15.dp
            ),
        contentScale = ContentScale.Crop,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.lighting(
            add = Color(0xFF4B1606),
            multiply = Color.Red
        )
    )

    Image(imageVector = Icons.Filled.Search, contentDescription ="Mm",
        modifier = Modifier
            .height(52.dp)
            .padding(
                horizontal = 8.dp,
                vertical = 15.dp
            ),
        contentScale = ContentScale.Crop,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.lighting(
            add = Color(0xFFFFFFFF),
            multiply = Color.White
        )
    )
}

@Composable
fun TopCategoryHome() {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categories") }
    val categories = listOf("TO", "DO")

    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(230.dp)
            .padding(
                horizontal = 8.dp,
                vertical = 5.dp
            )
    ){
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        )
        {
            Text(
                text = "TV Shows",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    )
            )
        }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        )
        {
            Text(
                text = "Movies",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    )
            )
        }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        )
        {
            Text(
                text = selectedCategory,
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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