package com.example.vflix

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : ComponentActivity() {
    private val movieViewModel: MovieViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val movies by movieViewModel.movies.observeAsState(emptyList())
            HomePage(titles = movies)
        }

        movieViewModel.fetchMovies()
    }
}




