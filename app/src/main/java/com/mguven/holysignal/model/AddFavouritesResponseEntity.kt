package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class AddFavouritesResponseEntity(
    @Json(name = "action") val action: String)