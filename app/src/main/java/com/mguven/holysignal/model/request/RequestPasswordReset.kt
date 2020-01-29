package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestPasswordReset(@Json(name = "email") val email: String)