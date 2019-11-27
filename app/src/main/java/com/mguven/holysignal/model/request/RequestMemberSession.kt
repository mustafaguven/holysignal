package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestMemberSession(@Json(name = "token") val token: String? = null)