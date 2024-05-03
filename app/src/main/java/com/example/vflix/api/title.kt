package com.example.vflix.api

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

var FeaturedItems = mutableStateOf(emptyList<FeaturedItem>())

var IsServerOffline = mutableStateOf(false)

class FeaturedItem(
    @com.google.gson.annotations.SerializedName("title") val title: String,
    @com.google.gson.annotations.SerializedName("href") val href: String,
    @com.google.gson.annotations.SerializedName("category") val category: String,
    @com.google.gson.annotations.SerializedName("poster") val poster: String,
    @com.google.gson.annotations.SerializedName("quality") val quality: String,
)

class NT {
    @com.google.gson.annotations.SerializedName("id")
    var id: String = ""
    var href: String = ""

    @com.google.gson.annotations.SerializedName("title")
    var title: String = ""

    @com.google.gson.annotations.SerializedName("description")
    var description: String = ""

    @com.google.gson.annotations.SerializedName("image")
    var image: String = ""

    @com.google.gson.annotations.SerializedName("quality")
    var quality: String = ""

    @com.google.gson.annotations.SerializedName("imdb_rating")
    var imdb_rating: String = ""

    @com.google.gson.annotations.SerializedName("genres")
    var genres: String = ""

    @com.google.gson.annotations.SerializedName("casts")
    var casts: String = ""

    @com.google.gson.annotations.SerializedName("duration")
    var duration: String = ""

    @com.google.gson.annotations.SerializedName("country")
    var country: String = ""

    @com.google.gson.annotations.SerializedName("production")
    var production: String = ""

    @com.google.gson.annotations.SerializedName("release")
    var release: String = ""

    @com.google.gson.annotations.SerializedName("similar_titles")
    var similar_titles: List<NTSearch> = listOf()

    @com.google.gson.annotations.SerializedName("trailer")
    var trailer: String = ""
    var category: String = ""
    var seasons: List<NSeason> = listOf()
    var episodes: List<NEpisode> = listOf()
    var e: String = ""
    var file: String = ""
    var genJS: Boolean = false
}

class NSeason {
    @com.google.gson.annotations.SerializedName("title")
    val title: String = ""

    @com.google.gson.annotations.SerializedName("season_id")
    val season_id: String = ""
}

class NEpisode {
    @com.google.gson.annotations.SerializedName("title")
    val title: String = ""

    @com.google.gson.annotations.SerializedName("episode_id")
    val episode_id: String = ""
}

class NSubtitle {
    @com.google.gson.annotations.SerializedName("file")
    var uri: String = ""

    @com.google.gson.annotations.SerializedName("label")
    var lang: String = ""

    var default: Boolean = false
}

fun urlquote(s: String): String {
    return s.replace(" ", "%20")
}

fun GatherFeaturedItems(
    featuredItems: MutableState<List<FeaturedItem>>,
    context: android.content.Context,
    isInit: Boolean
) {
    Thread {
        retrieveHomePageItemFromInternalStorage(context)
        if (FeaturedItems.value.isNotEmpty() && isInit) {
            featuredItems.value = FeaturedItems.value
            // wait 10 seconds before fetching again
            Thread.sleep(10000)
        }

        val client = OkHttpClient()
        val request =
            Request.Builder()
                .url("$BACKEND_URL/api/featured")
                .build()

        client
            .newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        var items: List<FeaturedItem>? = null;
                        try {
                            items =
                                Gson().fromJson(json, Array<FeaturedItem>::class.java).toList()
                        } catch (_: Exception) {
                            IsServerOffline.value = true
                        }
                        if (items != null) {
                            items = items.shuffled()
                        }
                        if (items != null) {
                            if (items.isNotEmpty()) {
                                FeaturedItems.value = items
                                saveHomePageItemToInternalStorage(context)
                            }
                        }
                    }
                }
            )
    }.start()
}

fun GatherNTInSync(
    href: String,
    m: MutableState<NT?>,
    se: MutableState<Pair<Int, Int>>,
    ActiveM3U8: MutableState<String>,
    Subs: MutableState<List<NSubtitle>>,
    showLoading: MutableState<Boolean>,
    showNoSource: MutableState<Boolean>
) {
    Thread {
        println("Gathering NT: $BACKEND_URL/api/info?id=${urlquote(href)}")
        val client = OkHttpClient()
        val request =
            Request.Builder().url("$BACKEND_URL/api/info?id=${urlquote(href)}").build()
        val response = client.newCall(request).execute()

        println("Gathering NT: $BACKEND_URL/api/info?id=${urlquote(href)}")
        val json = response.body?.string()
        val items = Gson().fromJson(json, NT::class.java)

        val titleId = href.split("-").last()
        println(href.split("/"))
        val titleType = href.split("/")[1]

        val NTObject = NT()
        NTObject.href = href

        NTObject.id = titleId
        NTObject.title = items.title
        NTObject.description = items.description
        NTObject.image = items.image
        NTObject.quality = items.quality
        NTObject.imdb_rating = items.imdb_rating.split(": ").last()
        NTObject.category = titleType
        NTObject.genres = items.genres
        NTObject.casts = items.casts
        NTObject.duration = items.duration
        NTObject.country = items.country
        NTObject.production = items.production
        NTObject.release = items.release
        NTObject.similar_titles = items.similar_titles
        NTObject.trailer = items.trailer

        m.value = NTObject

        if (titleType == "tv") {
            println("Gathering seasons for $titleId: $BACKEND_URL/api/seasons?id=${titleId}")
            val seasonRequest =
                Request.Builder().url("$BACKEND_URL/api/seasons?id=${titleId}").build()
            val seasonResponse = client.newCall(seasonRequest).execute()
            val seasonJson = seasonResponse.body?.string()
            val seasons =
                Gson().fromJson(seasonJson, Array<NSeason>::class.java).toList()
            NTObject.seasons = seasons
            if (NTObject.seasons.isNotEmpty()) {
                se.value = Pair(NTObject.seasons[0].season_id.toInt(), 0)
                GatherEpisodes(m, se, ActiveM3U8, Subs, showLoading, showNoSource)
            }
        } else {
            se.value = Pair(titleId.toInt(), titleId.toInt())
            GatherEmbedURL(m, se, ActiveM3U8, Subs, showLoading, showNoSource)
        }
    }.start()
}

fun GatherEpisodes(
    a: MutableState<NT?>,
    se: MutableState<Pair<Int, Int>>,
    ActiveM3U8: MutableState<String>,
    Subs: MutableState<List<NSubtitle>>,
    showLoading: MutableState<Boolean>,
    showNoSource: MutableState<Boolean>,
    showFetchEmbed: Boolean = true
) {
    println("Gathering episodes for ${a.value?.id}: $BACKEND_URL/api/episodes?id=${se.value.first}")
    Thread {
        val client = OkHttpClient()
        val request =
            Request.Builder().url("$BACKEND_URL/api/episodes?id=${se.value.first}").build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val items = Gson().fromJson(json, Array<NEpisode>::class.java).toList()
        a.value?.episodes = items

        if (se.value.second == 0) {
            if (a.value?.episodes?.size!! > 0) {
                se.value = Pair(se.value.first, a.value?.episodes?.get(0)?.episode_id?.toInt()!!)
                println("Active episode: ${se.value.second}")
                if (showFetchEmbed) {
                    GatherEmbedURL(a, se, ActiveM3U8, Subs, showLoading, showNoSource)
                }
            }
        }
    }.start()
}

fun GatherEmbedURL(
    a: MutableState<NT?>,
    se: MutableState<Pair<Int, Int>>,
    ActiveM3U8: MutableState<String>,
    Subs: MutableState<List<NSubtitle>>,
    showLoading: MutableState<Boolean>,
    showNoSource: MutableState<Boolean>,
    retries: Int = 0
) {
    Thread {
        try {
            showLoading.value = true
            val MAX_RETRIES = 3
            println("Gathering embed URL: $BACKEND_URL/api/embed?id=${se.value.second}&cat=${a.value?.category}")
            val client = OkHttpClient().newBuilder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url("$BACKEND_URL/api/embed?id=${se.value.second}&cat=${a.value?.category}")
                .build()
            val response = client.newCall(request).execute()

            val json = response.body?.string()
            val items = Gson().fromJson(json, Map::class.java)
            if (items["source_hash"] != null) {
                a.value?.e = items["source_hash"].toString()

                if (a.value?.e != "") {
                    val req = Request.Builder()
                        .url("$BACKEND_URL/${a.value?.e}")
                        .build()

                    val res = client.newCall(req).execute()
                    val j = res.body?.string()
                    val i = Gson().fromJson(j, Map::class.java)
                    if (i["file"] != null) {
                        ActiveM3U8.value = i["file"].toString()
                        println("ActiveM3U8: ${ActiveM3U8.value}")
                    }

                    if (i["subs"] != null) {
                        println("Subtitles: ${i["subs"]}")
                        var subs = mutableListOf<NSubtitle>()
                        val sub = i["subs"] as List<*>?

                        if (sub != null) {
                            for (s in sub) {
                                val subMap = s as Map<*, *>
                                subs.add(NSubtitle().apply {
                                    uri = subMap["file"].toString()
                                    lang = subMap["label"].toString()

                                    if (subMap["default"] != null) {
                                        default = true
                                    }
                                })
                            }
                        }

                        Subs.value = subs
                    }
                }
            } else {
                ActiveM3U8.value = ""
                println("retries: $retries / $MAX_RETRIES, retrying")
                if (retries < MAX_RETRIES) {
                    GatherEmbedURL(a, se, ActiveM3U8, Subs, showLoading, showNoSource, retries + 1)
                } else {
                    showNoSource.value = true
                }
            }
            showLoading.value = false
        } catch (e: Exception) {
            println("error: $e")
            showLoading.value = false
        }
    }.start()
}

fun FetchTrailer(e: String): String {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url("$BACKEND_URL/api/trailer?url=${e}").build()
    val response = client.newCall(request).execute()
    val json = response.body?.string()
    val items = Gson().fromJson(json, Map::class.java)
    if (items["url"] != null) {
        println("Trailer URL: ${items["url"].toString()}")
        return items["url"].toString()
    }
    return ""
}

class NTQuality {
    @com.google.gson.annotations.SerializedName("quality")
    var quality: String = ""

    @com.google.gson.annotations.SerializedName("url")
    var url: String = ""
}

fun M3U8QualitiesSync(m3u8_url: String, q: MutableState<List<NTQuality>>) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(m3u8_url)
        .build()

    val response = client.newCall(request).execute()
    val body = response.body?.string()
    if (body != null) {
        val lines = body.split("\n")
        val qualities = mutableListOf<NTQuality>()
        for (line in lines) {
            if (line.contains("RESOLUTION")) {
                val name = line.split("RESOLUTION=")[1].split(",")[0]
                val url = lines[lines.indexOf(line) + 1]
                if (!url.contains("#EXT-X") && url.contains(".m3u8")) {
                    qualities.add(NTQuality().apply {
                        quality = name
                        this.url = m3u8_url.split("index.m3u8")[0] + url
                    })
                }
            }
        }
        q.value = qualities
    }
}

fun m3u8ToQualities(m3u8_url: String) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(m3u8_url)
        .build()

    client.newCall(request).enqueue(
        object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("failed to get mpd")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                if (body != null) {
                    val lines = body.split("\n")
                    for (line in lines) {
                        if (line.contains("RESOLUTION")) {
                            val name = line.split("RESOLUTION=")[1].split(",")[0]
                            val url = lines[lines.indexOf(line) + 1]
                            if (!url.contains("#EXT-X") && url.contains(".m3u8")) {
                                println("Quality: $name, ${m3u8_url.split("index.m3u8")[0] + url}")
                                //q.add(Quality(name, m3u8_url.split("index.m3u8")[0] + url))
                            }
                        }
                    }
                }

                //println("XQualities: $q")
            }
        }
    )
}