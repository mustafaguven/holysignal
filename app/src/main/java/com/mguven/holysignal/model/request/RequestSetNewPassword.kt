package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestSetNewPassword(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)