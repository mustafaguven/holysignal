package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class SignInResponseEntity(
    @Json(name = "name") val name: String,
    @Json(name = "surname") val surname: String,
    @Json(name = "token") val token: String)