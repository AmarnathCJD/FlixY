package com.example.vflix.parser

import com.example.vflix.TmdbFind
import com.google.gson.Gson
import okhttp3.OkHttpClient

fun runtimeFormatter(runtime: Int): String {
    val hours = runtime / 60
    val minutes = runtime % 60
    if (hours == 0) {
        return "${minutes}m"
    }
    return "${hours}h ${minutes}m"
}

fun revRuntime(runtime: String): Int {
    val hours = runtime.substring(0, runtime.indexOf("h")).trim().toInt()
    val minutes = runtime.substring(runtime.indexOf("h") + 1, runtime.indexOf("m")).trim().toInt()
    return hours * 60 + minutes
}

fun getRemainingTime(runtime: Int, progress: Int): String {
    val remaining = runtime - progress
    val hours = remaining / 60
    val minutes = remaining % 60
    if (hours == 0) {
        return "${minutes}m"
    }
    return "${hours}h ${minutes}m"
}

fun getYear(date: String): String {
    return date.substring(0, 4)
}

fun abbrivateCountryIfLong(country: String): String {
    if (country.length > 15) {
        val words = country.split(" ")
        var abbr = ""
        words.forEach { word ->
            if (word[0].isUpperCase()) {
                abbr += word[0]
            }
        }
        return abbr
    }
    return country
}

fun getTVRuntime(data: TmdbData) : String {
    var runtime = 0
    if (data.lastEpisodeToAir != null) {
        runtime = data.lastEpisodeToAir.runtime
    } else {
        runtime = data.runtime
    }
    return runtimeFormatter(runtime)
}

fun ItoT(iD: String) : String {
    val url =
        "https://api.themoviedb.org/3/find/$iD?&external_source=imdb_id&api_key=d56e51fb77b081a9cb5192eaaa7823ad"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    val resp = client.newCall(request).execute()
    val body = resp.body?.string()
    val tmdbFind = Gson().fromJson(body, TmdbFind::class.java)
    var tId = ""
    if (tmdbFind.movieResults.isNotEmpty()) {
        tId = tmdbFind.movieResults[0].id.toString()
    } else if (tmdbFind.tvResults.isNotEmpty()) {
        tId = tmdbFind.tvResults[0].id.toString()
    }

    return tId
}