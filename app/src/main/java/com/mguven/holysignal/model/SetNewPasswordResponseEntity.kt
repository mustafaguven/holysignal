package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SetNewPasswordResponseEntity(
    @Json(name = "action") val action: String)