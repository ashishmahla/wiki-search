package com.android.apps.wikisearch.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.apps.wikisearch.apis.ApiInterface
import com.android.apps.wikisearch.apis.BASE_URL
import com.android.apps.wikisearch.models.SearchPrediction
import com.android.apps.wikisearch.models.SearchPredictionDeserializer
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainViewModel : ViewModel() {
    private val _searchPrediction: MutableLiveData<SearchPrediction> = MutableLiveData()

    private var searchPredictionJob: Job? = null
    private var isJobWaitingForTimeout = false

    var isSubmitted: Boolean = false
        set(value) {
            if (value) {
                searchPredictionJob?.cancel()
                _searchPrediction.value = null
            }

            field = value
        }

    val searchPrediction: LiveData<SearchPrediction> = _searchPrediction
    var searchResult: SearchPrediction? = null

    var searchQuery: String? = null
        set(value) {
            field = value
            resetSearchPredictionsJob()
        }

    private fun resetSearchPredictionsJob() {
        if (!isJobWaitingForTimeout) {
            searchPredictionJob?.cancel()
            searchPredictionJob = this.viewModelScope.launch {
                isJobWaitingForTimeout = true

                try {
                    var sq: String? = null

                    while (sq != searchQuery) {
                        sq = searchQuery
                        delay(500)
                    }

                    isJobWaitingForTimeout = false
                    if (!sq.isNullOrBlank()) {
                        _searchPrediction.value = withContext(Dispatchers.IO) {
                            try {
                                getSearchPredictions(sq)
                            } catch (exp: Exception) {
                                exp.printStackTrace()
                                null
                            }
                        }
                    } else {
                        _searchPrediction.value = null
                    }
                } finally {
                    isJobWaitingForTimeout = false
                }
            }
        }
    }

    suspend fun getSearchPredictions(searchQuery: String?): SearchPrediction {
        val gson = GsonBuilder().registerTypeAdapter(
            SearchPrediction::class.java,
            SearchPredictionDeserializer()
        ).create()

        /*val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(getApplication<Application>().cacheDir, 5 * 1_024 * 1_024))
            .addInterceptor { chain ->
                val request =
                    chain.request().newBuilder()
                        .header("Cache-Control", "public, max-age=" + 5 * 60 * 60)
                        .build()
                chain.proceed(request)
            }
            .build()*/


        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            //.client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(ApiInterface::class.java)

        return suspendCoroutine {
            service.searchPredictions(searchQuery).enqueue(object : Callback<SearchPrediction> {
                override fun onResponse(
                    call: Call<SearchPrediction>,
                    response: Response<SearchPrediction>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        it.resume(body)
                    } else {
                        it.resumeWithException(
                            Exception(
                                "CallResult: isSuccessful: ${response.isSuccessful}, " +
                                        "body: ${response.body()}, errorBody: ${response.errorBody()}"
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<SearchPrediction>, t: Throwable) {
                    it.resumeWithException(t)
                }
            })
        }
    }
}