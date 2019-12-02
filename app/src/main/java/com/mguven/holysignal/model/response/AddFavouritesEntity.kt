package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.AddFavouritesResponseEntity
import com.mguven.holysignal.model.SurahResponseEntity
import com.squareup.moshi.Json

data class AddFavouritesEntity(
    @Json(name = "data") override val data: AddFavouritesResponseEntity? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface