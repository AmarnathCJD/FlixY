package com.example.vflix.api

import android.content.Context
import com.google.gson.Gson
import java.io.File

fun saveHomePageItemToInternalStorage(context: Context) {
    try {
        val file = File(context.filesDir, "home_page.json")

        val data = Gson().toJson(
            FeaturedItems.value
        )

        file.writeText(data)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun retrieveHomePageItemFromInternalStorage(context: Context) {
    try {
        val file = File(context.filesDir, "home_page.json")
        val data = file.readText()

        val items = Gson().fromJson(data, Array<FeaturedItem>::class.java).toList()
        if (items.isNotEmpty()) {
            FeaturedItems.value = items
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}