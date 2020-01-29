package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestSignUp(@Json(name = "name") val name: String,
                         @Json(name = "surname") val surname: String,
                         @Json(name = "email") val email: String,
                         @Json(name = "password") val password: String,
                         @Json(name = "sessionno") val sessionno: String?)