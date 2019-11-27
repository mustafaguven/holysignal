package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.SurahTranslateEntity
import com.squareup.moshi.Json

data class SurahTranslateResponseEntity(
    @Json(name = "data") override val data: SurahTranslateEntity? = null,
    override val message: String,
    override val status: Int) : ResponseBaseEntityInterface