package com.example.vflix

import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.vflix.auth.DotsPreview
import com.example.vflix.ui.theme.sans_bold

var appUserId = "1151584573787594752"

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            /*var navController = rememberNavController()
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
            }
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
            Thread.sleep(2000)
            showStartLogo = false
        }.start()
             */

            LiveTV()
        }
    }
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
                .background(Color.Transparent)
                ,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column (
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ){
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