package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SurahEntity(
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: String? = null,
    @Json(name = "status") val status: Int? = null)