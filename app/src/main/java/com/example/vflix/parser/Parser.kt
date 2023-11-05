package com.example.vflix.parser

import androidx.compose.runtime.MutableState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

data class MediaSrc(
    @com.google.gson.annotations.SerializedName("quality") val quality: String,
    @com.google.gson.annotations.SerializedName("url") val url: String
)

data class CDN(
    @com.google.gson.annotations.SerializedName("media") val sources: List<MediaSrc>,
    @com.google.gson.annotations.SerializedName("tmdb_id") val tmdbID: String,
)

fun CDN.getBest(): String {
    var best = ""
    var bestQuality = 0
    for (source in this.sources) {
        if (source.quality != "auto") {
            if (source.quality.toInt() > bestQuality) {
                best = source.url
                bestQuality = source.quality.toInt()
            }
        }
    }
    return best
}

fun fetchAndExtractSources(
    tmdbID: String,
    type: String,
    season: Int,
    episode: Int,
    src: MutableState<CDN>,
    isActive: MutableState<Boolean>
) {

    if (tmdbID.contains("tt")) {
        return imdbToTmdbID(tmdbID, type, season, episode, src, isActive)
    }
    if (isActive.value) {
        return
    }
    if (!tmdbID.isNullOrEmpty() && !type.isNullOrEmpty()) {
        isActive.value = true
    }



    println("STRANGE params: $tmdbID, $type, $season, $episode")
    var url = "https://embed.smashystream.com/playere.php?tmdb=$tmdbID"
    if (type.contains("TV")) {
        url += "&season=$season&episode=$episode"
    }

    val client = OkHttpClient()

    val request = okhttp3.Request.Builder()
        .url(url)
        .headers(
            Headers.Builder()
                .add(
                    "User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0"
                )
                .add("Referer", "https://embed.smashystream.com/playere.php?tmdb=$tmdbID")
                .build()
        )
        .build()

    client.newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    println("REQW: ${response.request.url}")
                    val body = response.body?.string()
                    if (body != null) {
                        // val doc = htmlDocument(body)
                        // regex https:\/\/embed.smashystream.com\/fizz(.*?).php\?tmdb=\d+(&season=\d+)?(&episode=\d+)?
                        val regex =
                            Regex("https:\\/\\/embed.smashystream.com\\/fizz(.*?).php\\?tmdb=\\d+(&season=\\d+)?(&episode=\\d+)?")
                        val match = regex.find(body)
                        if (match != null) {
                            println("MatchVV: ${match.value}")
                            val url = match.value
                            val client = OkHttpClient()
                            val request = okhttp3.Request.Builder()
                                .url(url)
                                .headers(
                                    Headers.Builder()
                                        .add(
                                            "User-Agent",
                                            "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0"
                                        )
                                        .add(
                                            "Referer",
                                            "https://embed.smashystream.com/playere.php?tmdb=$tmdbID"
                                        )
                                        .build()
                                )
                                .build()

                            client.newCall(request)
                                .enqueue(
                                    object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            println("Error, ${e.message}")
                                            e.printStackTrace()
                                            isActive.value = false
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            try {
                                                val regex = Regex("\"file\":\"(.*?)\"")
                                                val matches = regex.findAll(response.body?.string()!!)
                                                for (match in matches) {
                                                    val regex2 = Regex("\\[(.*?)\\](.*?),")
                                                    val matches2 = regex2.findAll(match.value)
                                                    val sources = mutableListOf<MediaSrc>()
                                                    for (match2 in matches2) {
                                                        val quality = match2.groupValues[1]
                                                        val url =
                                                            match2.groupValues[2].replace("\\", "")
                                                        sources.add(MediaSrc(quality, url))
                                                    }
                                                    src.value = CDN(sources, tmdbID)
                                                    isActive.value = false
                                                    println("MSRC: $sources")
                                                }
                                            } catch (e: Exception) {
                                                println("Error, ${e.message}")
                                                e.printStackTrace()
                                            }
                                        }
                                    })
                        } else {
                            isActive.value = false
                        }

                    }
                }
            })
}


fun imdbToTmdbID(tmdbID: String, type: String, season: Int, episode: Int, src: MutableState<CDN>, isActive: MutableState<Boolean>) {
    val url =
        "https://api.themoviedb.org/3/find/$tmdbID?api_key=d56e51fb77b081a9cb5192eaaa7823ad&external_source=imdb_id"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client.newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error, ${e.message}")
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = com.google.gson.Gson()
                            .fromJson(body, com.google.gson.JsonObject::class.java)
                        if (json["movie_results"].asJsonArray.size() > 0) {
                            val tmdbID =
                                json["movie_results"].asJsonArray[0].asJsonObject["id"].asString
                            return fetchAndExtractSources(tmdbID, type, season, episode, src, isActive)
                        } else if (json["tv_results"].asJsonArray.size() > 0) {
                            val tmdbID =
                                json["tv_results"].asJsonArray[0].asJsonObject["id"].asString
                            return fetchAndExtractSources(tmdbID, type, season, episode, src, isActive)
                        } else {
                            return
                        }
                    }
                }
            })
}
