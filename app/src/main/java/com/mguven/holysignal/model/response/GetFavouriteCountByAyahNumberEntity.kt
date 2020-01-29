package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

data class GetFavouriteCountByAyahNumberEntity(
    @Json(name = "data") override val data: Int? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface