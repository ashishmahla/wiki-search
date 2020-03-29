package com.android.apps.wikisearch.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

data class SearchPrediction(
    val pages: List<Page>
)

data class Page(
    val pageId: Int,
    val title: String,
    val index: Int,
    val description: String?,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String,
    val width: Int,
    val height: Int
)

class SearchPredictionDeserializer : JsonDeserializer<SearchPrediction> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SearchPrediction? {
        return if (json == null) null
        else try {
            json as JsonObject
            val pagesArray = json.getAsJsonObject("query").getAsJsonArray("pages")
            val pageList = mutableListOf<Page>()

            for (pageJson in pagesArray) {
                pageJson as JsonObject

                val page = Page(
                    pageId = pageJson.get("pageid")?.asInt ?: 0,
                    title = pageJson.get("title")?.asString ?: "",
                    index = pageJson.get("index")?.asInt ?: 0,

                    description = pageJson.getAsJsonObject("terms")
                        ?.getAsJsonArray("description")?.let { descArray ->
                            if (descArray.count() > 0) descArray.get(0).asString
                            else null
                        },

                    thumbnail = pageJson.getAsJsonObject("thumbnail")?.let {
                        Thumbnail(
                            it.get("source").asString,
                            it.get("height").asInt,
                            it.get("width").asInt
                        )
                    }
                )

                pageList.add(page)
            }

            SearchPrediction(pageList)

        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }
}