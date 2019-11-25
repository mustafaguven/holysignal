package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SurahTranslateResponseEntity(
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: SurahTranslateEntity? = null,
    @Json(name = "status") val status: Int? = null)