package com.example.vflix.parser

import androidx.compose.runtime.MutableState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

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
    var url = "https://embed.smashystream.com/playere.php?tmdb=$tmdbID"
    if (type.contains("TV")) {
        url += "&season=$season&episode=$episode"
    }

    println("MSRC: $url")

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
                    val body = response.body?.string()
                    if (body != null) {
                        /*val regex =
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
                        }*/


                        val pattern = Pattern.compile("""data-url="([^"]+)"""")
                        val matcher = pattern.matcher(body)

                        val servers = mutableListOf<String>()

                        while (matcher.find()) {
                            val dataUrl = matcher.group(1)
                            if (!dataUrl.contains("_default")) {
                                servers.add(dataUrl)
                            }
                        }

                        val sources = mutableListOf<MediaSrc>()
                        while (servers.size > 0) {
                            val server = servers.removeFirst()
                            val serverSources = extractSecondaryServer(server)
                            sources.addAll(serverSources)

                            if (serverSources.isNotEmpty()) {
                                break
                            }

                            Thread.sleep(1000)
                        }

                        src.value = CDN(sources, tmdbID)
                        isActive.value = false
                    }
                }
            })
}

fun extractSecondaryServer(url: String): List<MediaSrc> {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .headers(
            Headers.Builder()
                .add(
                    "User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0"
                )
                .build()
        )
        .build()

    val req = client.newCall(request)
        .execute()

    var body = req.body?.string() ?: return listOf()
    body = body.replace("<br", "")
    var jsonObject: JSONObject
    try {
        jsonObject = JSONObject(body)
    } catch (e: Exception) {
        return listOf()
    }
    val sourceUrls = jsonObject.getJSONArray("sourceUrls")

    //val subtitleUrls = jsonObject.getString("subtitleUrls")

    val sources = mutableListOf<MediaSrc>()

    if (sourceUrls.length() == 0) {
        return sources
    }

    for (i in 0 until sourceUrls.length()) {
        try {
            val source = sourceUrls[i].toString()
            val quality = "1080"
            if (source != null && source != "" && source != "null") {
                sources.add(MediaSrc(quality, replaceEscapeChars(source)))
            }
        } catch (e: Exception) {
            println("vFlix - Error, ${e.message}")
        }
    }

    return sources
}

fun replaceEscapeChars(str: String): String {
    return str.replace("\\", "")
}

fun imdbToTmdbID(
    tmdbID: String,
    type: String,
    season: Int,
    episode: Int,
    src: MutableState<CDN>,
    isActive: MutableState<Boolean>
) {
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
                            return fetchAndExtractSources(
                                tmdbID,
                                type,
                                season,
                                episode,
                                src,
                                isActive
                            )
                        } else if (json["tv_results"].asJsonArray.size() > 0) {
                            val tmdbID =
                                json["tv_results"].asJsonArray[0].asJsonObject["id"].asString
                            return fetchAndExtractSources(
                                tmdbID,
                                type,
                                season,
                                episode,
                                src,
                                isActive
                            )
                        } else {
                            return
                        }
                    }
                }
            })
}

data class Channel(
    @com.google.gson.annotations.SerializedName("name") val name: String,
    @com.google.gson.annotations.SerializedName("image") val image: String,
    @com.google.gson.annotations.SerializedName("genre") val genre: String,
    @com.google.gson.annotations.SerializedName("id") val id: String,
)


var MainChannels = mutableListOf<Channel>()

fun FetchChannels() {
    val url = "https://live.csa.codes/api/search?all=true"
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client.newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body != null) {
                        val channels = com.google.gson.Gson()
                            .fromJson(body, com.google.gson.JsonArray::class.java)
                        for (channel in channels) {
                            val name = channel.asJsonObject["name"].asString
                            val image = channel.asJsonObject["image"].asString
                            val genre = channel.asJsonObject["genre"].asString
                            val id = channel.asJsonObject["id"].asString
                            MainChannels.add(Channel(name, image, genre, id))
                        }
                    }
                }
            })
}

fun SearchChannel(query: String, channelsQ: MutableState<List<Channel>>) {
    val temp = mutableListOf<Channel>()
    for (channel in MainChannels) {
        if (channel.name.contains(query, ignoreCase = true)) {
            temp.add(channel)
        }
    }

    channelsQ.value = temp
}

data class DRM(
    val name: String,
    val mpd: String,
    @com.google.gson.annotations.SerializedName("key") val keyId: String,
    val key: String,
    val id: String,
    val curr: String,
)

fun GetDRM(channelID: String, drm: MutableState<DRM>) {
    val url = "https://live.csa.codes/api/drm?id=$channelID"
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
                        var curr = ""; var name = ""; var mpd = ""; var keyId = ""; var key = ""
                        if (json.has("name")) {
                            name = json["name"].asString
                        }
                        if (json.has("mpd")) {
                            mpd = json["mpd"].asString
                        }
                        if (json.has("key_id")) {
                            keyId = json["key_id"].asString
                        }
                        if (json.has("key")) {
                            key = json["key"].asString
                        }
                        if (json.has("current_program")) {
                            curr = json["current_program"].asString
                        }
                        drm.value = DRM(name, mpd, keyId, key, channelID, curr)
                    }
                }
            })
}
