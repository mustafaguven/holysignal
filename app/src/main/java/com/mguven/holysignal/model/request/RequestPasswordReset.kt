package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestAddOrder(@Json(name = "memberId") val memberId: Int)