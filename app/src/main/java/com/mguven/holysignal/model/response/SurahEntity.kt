package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.SurahResponseEntity
import com.squareup.moshi.Json

data class SurahEntity(
    @Json(name = "data") override val data: SurahResponseEntity? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface