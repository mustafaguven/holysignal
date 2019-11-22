package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SurahResponseEntity(
    @Json(name = "surahNumber") val surahNumber: String,
    @Json(name = "editionId") val editionId: String,
    @Json(name = "ayahs") val ayahs: List<AyahResponseEntity>)