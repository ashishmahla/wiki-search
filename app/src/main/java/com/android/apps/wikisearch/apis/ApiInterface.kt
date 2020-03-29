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
                "&prop=info%7Cpageimages%7Cpageterms" +
                "&inprop=url" +
                "&generator=prefixsearch" +
                "&redirects=1" +
                "&formatversion=2" +
                "&piprop=thumbnail" +
                "&pithumbsize=200" +
                "&pilimit=10" +
                "&wbptterms=description" +
                "&gpslimit=17"
    )
    fun searchPredictions(@Query("gpssearch") searchQuery: String?): Call<SearchPrediction>
}