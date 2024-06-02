package com.example.vflix.api

import androidx.compose.runtime.MutableState
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
class NTSearchBox(
    @com.google.gson.annotations.SerializedName("next") val next: String,
    @com.google.gson.annotations.SerializedName("titles") val titles: List<NTSearch>,
)
class NTSearch(
    @com.google.gson.annotations.SerializedName("title") val title: String,
    @com.google.gson.annotations.SerializedName("href") val href: String,
    @com.google.gson.annotations.SerializedName("category") val category: String,
    @com.google.gson.annotations.SerializedName("poster") var poster: String,
    @com.google.gson.annotations.SerializedName("quality") val quality: String,
)

fun ImdbTop(
    data: MutableState<List<NTSearch>>,
    showProgress: MutableState<Boolean>,
    showError: MutableState<Boolean>,
    errorMess: MutableState<String>
) {
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url("$BACKEND_URL/api/imdb")
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle network request failure
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    var resp: Array<NTSearch>? = null
                    try {
                        resp = Gson().fromJson(json, Array<NTSearch>::class.java)
                    } catch (e: Exception) {
                        println("Error: $e")
                        showProgress.value = false
                        showError.value = true
                        errorMess.value = "Server Error (500)"
                        return
                    }

                    if (resp != null) {
                        data.value = resp.toList()
                    }

                    // set poster field suffix as BACKEND_URL
                    for (i in data.value.indices) {
                        data.value[i].poster = "$BACKEND_URL/api/img?url=${data.value[i].poster}"
                    }

                    showProgress.value = false
                }
            }
        )
}

fun INTSearchTitle(query: String, data: MutableState<List<NTSearch>>, showProgress: MutableState<Boolean>, showNoResults: MutableState<Boolean>) {
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url("$BACKEND_URL/api/sa?query=${urlquote(query)}")
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle network request failure
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    var resp: NTSearchBox? = null
                    try {
                        resp = Gson().fromJson(json, NTSearchBox::class.java)
                    } catch (e: Exception) {
                        println("Error: $e")
                    }

                    if (resp != null) {
                        data.value = resp.titles
                    }

                    // set poster field suffix as BACKEND_URL
                    for (i in data.value.indices) {
                        data.value[i].poster = "$BACKEND_URL/api/img?url=${data.value[i].poster}"
                    }

                    if (resp != null) {
                        if (resp.titles.isEmpty()) {
                            showNoResults.value = true
                        } else {
                            showNoResults.value = false
                        }
                    }

                    showProgress.value = false
                }
            }
        )
}