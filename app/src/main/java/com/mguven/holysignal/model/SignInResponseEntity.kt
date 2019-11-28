package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SignInResponseEntity(
    @Json(name = "token") val token: String)