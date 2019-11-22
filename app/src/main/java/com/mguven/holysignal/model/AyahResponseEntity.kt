package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class AyahResponseEntity(
    @Json(name = "Id") val Id: Int,
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int)