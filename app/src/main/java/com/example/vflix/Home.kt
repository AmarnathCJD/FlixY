package com.example.vflix

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api

@Composable
fun MyAppHome() {
    //LoginForm()
    HomePage()
}

data class CarouselItem(
    val title: String,
    val subtitle: String,
    val image: Int
)

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomePage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val items = listOf(
            CarouselItem(
                title = "Title 1",
                subtitle = "Subtitle 1",
                image = R.drawable.logo
            ),
            CarouselItem(
                title = "Title 2",
                subtitle = "Subtitle 2",
                image = R.drawable.logo
            ),
        )
        Carousel(
            slideCount = 2,
            content = { index ->
                items[index].also { item ->
                    CarouselItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        content = {
                            Image(
                                painter = painterResource(R.drawable.max_quilt_0),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.width(100.dp).padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    )
                }
            }
        )
    }
}
