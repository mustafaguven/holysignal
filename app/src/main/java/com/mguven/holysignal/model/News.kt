package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class News(
    @Json(name = "status") val status: String? = null,
    @Json(name = "totalResults") val totalResults: Int? = null,
    @Json(name = "articles") val articles: List<Article>? = null)