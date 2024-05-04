package com.example.vflix.api

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import java.io.File

val CWAlreadyRead = mutableStateOf(false)

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

class ContinueWatchingEntry(
    val title: String,
    val id: String,
    val mediaType: String,
    val poster: String,
    val href: String ,

    var seasons: List<CWSeason>,
)

class CWSeason(
    val season_id: String,
    var episodes: List<CWEpisode>,
)

class CWEpisode(
    val ep_id: String,
    var position: Int,
)

val ContinueWatching: MutableState<List<ContinueWatchingEntry>> = mutableStateOf(
    emptyList()
)

// ------------------------- Continue Watching -------------------------
fun GetLastPlayedEpisode(id: String, mediaType: String, context: Context): CWEpisode? {
    if (!CWAlreadyRead.value) {
        println("retrieving continue watching from internal storage")
        retrieveContinueWatchingFromInternalStorage(context)
        CWAlreadyRead.value = true
    }
    println("CWid: $id, mediaType: $mediaType, cw: ${ContinueWatching.value}")
    val cw = ContinueWatching.value.find { it.id == id && it.mediaType == mediaType } ?: return null

    if (mediaType == "movie") return cw.seasons.first().episodes.maxByOrNull { it.position }
    return cw.seasons.flatMap { it.episodes }?.maxByOrNull { it.position }
}

fun RemoveLastPlayedEpisode(id: String, mediaType: String, context: Context) {
    if (!CWAlreadyRead.value) {
        println("retrieving continue watching from internal storage")
        retrieveContinueWatchingFromInternalStorage(context)
        CWAlreadyRead.value = true
    }
    val index = ContinueWatching.value.indexOfFirst { it.id == id && it.mediaType == mediaType }
    if (index != -1) {
        ContinueWatching.value = ContinueWatching.value.toMutableList().apply {
            removeAt(index)
        }
    }
    saveContinueWatchingToInternalStorage(context)
}

fun GetLastPlayedSeason(id: String, mediaType: String, context: Context): CWSeason? {
    if (!CWAlreadyRead.value) {
        println("retrieving continue watching from internal storage")
        retrieveContinueWatchingFromInternalStorage(context)
        CWAlreadyRead.value = true
    }
    val cw = ContinueWatching.value.find { it.id == id && it.mediaType == mediaType } ?: return null

    return cw.seasons.maxByOrNull { it.season_id.toInt() }
}

fun GetPositions(id: String, mediaType: String): List<CWEpisode> {
    val cw = ContinueWatching.value.find { it.id == id && it.mediaType == mediaType }
        ?: return emptyList()

    return cw.seasons.flatMap { it.episodes }
}

fun InsertOrUpdateLastPlayedEpisode(
    nt: NT,
    season: Int,
    episode: Int,
    position: Int,
    context: Context
) {
    if (!CWAlreadyRead.value) {
        println("retrieving continue watching from internal storage")
        retrieveContinueWatchingFromInternalStorage(context)
        CWAlreadyRead.value = true
    }
    // if no such title found, insert new, else if no such season found insert, else if no such episode found insert, else update
    val cw = ContinueWatching.value.find { it.id == nt.id && it.mediaType == nt.category }
        ?: ContinueWatchingEntry(
            nt.title,
            nt.id,
            nt.category,
            nt.image,
            nt.href,
            listOf(
                CWSeason(
                    season.toString(),
                    listOf(
                        CWEpisode(episode.toString(), position)
                    )
                )
            )
        )

    val seasonIndex = cw.seasons.indexOfFirst { it.season_id == season.toString() }
    if (seasonIndex == -1) {
        cw.seasons += CWSeason(
            season.toString(),
            listOf(
                CWEpisode(episode.toString(), position)
            )
        )
    } else {
        val episodeIndex =
            cw.seasons[seasonIndex].episodes.indexOfFirst { it.ep_id == episode.toString() }
        if (episodeIndex == -1) {
            cw.seasons[seasonIndex].episodes += CWEpisode(episode.toString(), position)
        } else {
            cw.seasons[seasonIndex].episodes[episodeIndex].position = position
        }
    }

    val index =
        ContinueWatching.value.indexOfFirst { it.id == nt.id && it.mediaType == nt.category }
    if (index == -1) {
        ContinueWatching.value += cw
    } else {
        ContinueWatching.value = ContinueWatching.value.toMutableList().apply {
            set(index, cw)
        }
    }

    saveContinueWatchingToInternalStorage(context)
}

fun saveContinueWatchingToInternalStorage(context: Context) {
    try {
        val file = File(context.filesDir, "continue_watching.json")

        val data = Gson().toJson(
            ContinueWatching.value
        )


        file.writeText(data)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun retrieveContinueWatchingFromInternalStorage(context: Context) {
    try {
        val file = File(context.filesDir, "continue_watching.json")
        val data = file.readText()

        val items = Gson().fromJson(data, Array<ContinueWatchingEntry>::class.java).toList()


        if (items.isNotEmpty()) {
            ContinueWatching.value = items
        }

        for (i in ContinueWatching.value.indices) {
            for (j in ContinueWatching.value[i].seasons.indices) {
                for (k in ContinueWatching.value[i].seasons[j].episodes.indices) {
                    println(
                        "id: ${ContinueWatching.value[i].id}, season: ${ContinueWatching.value[i].seasons[j].season_id}, episode: ${ContinueWatching.value[i].seasons[j].episodes[k].ep_id}, position: ${ContinueWatching.value[i].seasons[j].episodes[k].position}"
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteContinueWatchingFromInternalStorage(context: Context) {
    try {
        val file = File(context.filesDir, "continue_watching.json")
        file.delete()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}