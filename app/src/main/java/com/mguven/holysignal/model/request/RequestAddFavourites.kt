package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestAddFavourites(@Json(name = "token") val token: String?,
                                @Json(name = "ayahNumber") val ayahNumber: Int,
                                @Json(name = "isAdd") val isAdd: Int = 1)