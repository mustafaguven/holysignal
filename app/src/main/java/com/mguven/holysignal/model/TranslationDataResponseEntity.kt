package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class TranslationDataResponseEntity(
    @Json(name = "Id") val Id: Int,
    @Json(name = "surahNumber") val surahNumber: Int,
    @Json(name = "name") val name: String,
    @Json(name = "meaning") val meaning: String,
    @Json(name = "specification") val specification: String?)