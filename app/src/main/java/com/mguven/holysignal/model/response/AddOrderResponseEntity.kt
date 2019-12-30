package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

data class AddOrderEntity(
    @Json(name = "action") val action: String)