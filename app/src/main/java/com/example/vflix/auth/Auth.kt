package com.example.vflix.auth

import com.example.vflix.TmdbTitleMin
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

data class UserData(
    var name: String,
    var email: String,
    var password: String,
    var watched: CopyOnWriteArrayList<Watched> = CopyOnWriteArrayList(),
    var watchlist: CopyOnWriteArrayList<Watchlist> = CopyOnWriteArrayList()
)

data class Watched(
    var name: String,
    var poster: String,
    var tmdbID: String,
    var imdbID: String,
    var type: String,
    var eps: MutableList<Ep> = mutableListOf(),
    var moviet: Long = 0,
)

data class Ep(
    var season: Int,
    var episode: Int,
    var time: Long,
)

data class Watchlist(
    var tmdbID: String,
    var poster: String,
    var name: String,
    var type: String,
)

class Auth {
    companion object {
        var user: UserData = UserData("", "", "")
        var lastSync: Long = 0
    }
}

class DB() {
    private val client = OkHttpClient()
    private val url = "https://jsonblob.com/api/jsonBlob/"

    fun put() {
        this.getsync()
        val dataStr = Gson().toJson(Auth.user)
        val body = dataStr.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .put(body)
            .build()

        client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("ErrorV, ${e.message}")
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        println("ResponseV: ${response.body?.string()}")
                    }
                }
            )
    }

    private fun getsync() {
        val resp = client.newCall(
            okhttp3.Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
        ).execute()
        val body = resp.body?.string()
        if (body != null) {
            val data = Gson().fromJson(body, UserData::class.java)
            Auth.user = data
            Auth.lastSync = System.currentTimeMillis()
        }
    }

    fun putSync() {
        val usr = Auth.user

        val dataStr = Gson().toJson(usr)
        val body = dataStr.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .put(body)
            .build()

        client.newCall(request)
            .execute()
    }

    fun get() {
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
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
                            val data = Gson().fromJson(body, UserData::class.java)
                            Auth.user = data
                            Auth.lastSync = System.currentTimeMillis()
                        }
                    }
                }
            )
    }

    fun getposterandname(id: String, type: String): Pair<String, String> {
        var tmdbId = ""
        var type = type
        if (id.contains("tt")) {
            val url =
                "https://api.themoviedb.org/3/find/$id?api_key=d56e51fb77b081a9cb5192eaaa7823ad&external_source=imdb_id"
            val request = okhttp3.Request.Builder()
                .url(url)
                .build()

            val body = client.newCall(request)
                .execute().body?.string()
            val json = Gson()
                .fromJson(body, com.google.gson.JsonObject::class.java)
            println("MoviObj: ${json["movie_results"]}")
            if (json["movie_results"].asJsonArray.size() > 0) {
                tmdbId =
                    json["movie_results"].asJsonArray[0].asJsonObject["id"].asInt.toString()
                type = "movie"
            } else if (json["tv_results"].asJsonArray.size() > 0) {
                tmdbId =
                    json["tv_results"].asJsonArray[0].asJsonObject["id"].asString
                type = "tv"
            }

        } else {
            tmdbId = id
        }

        try {
            val url =
                "https://api.themoviedb.org/3/$type/$tmdbId?api_key=d56e51fb77b081a9cb5192eaaa7823ad&language=en-US"
            val request = okhttp3.Request.Builder()
                .url(url)
                .build()

            val body = client.newCall(request)
                .execute().body?.string()

            val json = Gson()
                .fromJson(body, TmdbTitleMin::class.java)

            val poster = "https://image.tmdb.org/t/p/original${json.poster}"
            val name = if (!json.name.isNullOrEmpty()) {
                json.name
            } else {
                json.title
            }
            return Pair(poster, name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair("", "")
    }
}

var db = DB()

fun addWatched(id: String, type: String, season: Int, episode: Int, time: Long) {
    if (!Auth.user.watched.isNullOrEmpty()) {
        for (i in Auth.user.watched.indices) {
            if ((Auth.user.watched[i].tmdbID == id || Auth.user.watched[i].imdbID == id) && Auth.user.watched[i].type == type) {
                for (j in Auth.user.watched[i].eps.indices) {
                    if (Auth.user.watched[i].eps[j].season == season && Auth.user.watched[i].eps[j].episode == episode) {
                        Auth.user.watched[i].eps[j].time = time
                        db.putSync()
                        return
                    }
                }
                Auth.user.watched[i].eps.add(Ep(season, episode, time))
                db.putSync()
                return
            }
        }
    }

    val (poster, name) = db.getposterandname(id, type)

    val watched = Watched(name, poster, "", "", type, mutableListOf(Ep(season, episode, time)))
    if (id.contains("tt")) {
        watched.imdbID = id
    } else {
        watched.tmdbID = id
    }

    if (Auth.user.watched.isNullOrEmpty()) {
        Auth.user.watched = CopyOnWriteArrayList()
        Auth.user.watched.add(watched)
    } else {
        Auth.user.watched.add(watched)
    }
    db.putSync()
}

fun updateSeek(
    id: String,
    season: Int,
    episode: Int,
    time: Long,
) {
    val ep = Ep(season, episode, time)
    if (!Auth.user.watched.isNullOrEmpty()) {
        for (i in Auth.user.watched.indices) {
            if ((Auth.user.watched[i].tmdbID == id || Auth.user.watched[i].imdbID == id)) {
                for (j in Auth.user.watched[i].eps.indices) {
                    if (Auth.user.watched[i].eps[j].season == season && Auth.user.watched[i].eps[j].episode == episode) {
                        Auth.user.watched[i].eps[j].time = time
                        db.putSync()
                        return
                    }
                }
                Auth.user.watched[i].eps.add(ep)
                db.putSync()
                return
            }
        }
    }
}


fun getPlaybackTime(id: String, season: Int, episode: Int): Long {
    if (!Auth.user.watched.isNullOrEmpty()) {
        for (watched in Auth.user.watched) {
            if ((watched.tmdbID == id || watched.imdbID == id) && watched.type.lowercase()
                    .contains("tv")
            ) {
                for (ep in watched.eps) {
                    if (ep.season == season && ep.episode == episode) {
                        return ep.time
                    }
                }
            } else if ((watched.tmdbID == id || watched.imdbID == id) && watched.type.lowercase()
                    .contains("movie")
            ) {
                return watched.moviet
            }
        }
    }
    return 0
}

fun getLastSE(id: String): Pair<Int, Int> {
    if (!Auth.user.watched.isNullOrEmpty()) {
        for (watched in Auth.user.watched) {
            if ((watched.tmdbID == id || watched.imdbID == id) && watched.type.lowercase()
                    .contains("tv")
            ) {
                val last = watched.eps.last()
                return Pair(last.season, last.episode)
            }
        }
    }
    return Pair(1, 1)
}

fun hasWatched(id: String): Boolean {
    if (!Auth.user.watched.isNullOrEmpty()) {
        for (watched in Auth.user.watched) {
            if (watched.tmdbID == id || watched.imdbID == id) {
                return true
            }
        }
    }
    return false
}


// periodic sync every 1 minutes
val sync = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
    while (false) {
        if (System.currentTimeMillis() - Auth.lastSync > 6000) {
            db.put()
        }
        println("SyncedP")
        kotlinx.coroutines.delay(6000)
    }
}
