package com.example.vflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

var passwordVisibility: Boolean = true
var isChecked = false

@Composable
fun MyApp() {
    //LoginForm()
    MyAppHome()
}



