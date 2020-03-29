package com.android.apps.wikisearch.apis

import com.android.apps.wikisearch.models.SearchPrediction
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://en.wikipedia.org/w/"

interface ApiInterface {
    @GET(
        "api.php?" +
                "action=query" +
                "&format=json" +
                "&prop=pageimages%7Cpageterms" +
                "&generator=prefixsearch" +
                "&redirects=1" +
                "&formatversion=2" +
                "&piprop=thumbnail" +
                "&pithumbsize=50" +
                "&pilimit=10" +
                "&wbptterms=description" +
                // "&gpssearch={searchQuery}" +
                "&gpslimit=10"
    )
    fun searchPredictions(@Query("gpssearch") searchQuery: String?): Call<SearchPrediction>
}