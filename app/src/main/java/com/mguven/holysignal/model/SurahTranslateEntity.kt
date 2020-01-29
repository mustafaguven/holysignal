package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SurahTranslateEntity(
    @Json(name = "languageId") val languageId: Int,
    @Json(name = "translationData") val translationData: List<TranslationDataResponseEntity>)