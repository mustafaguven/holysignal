package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestUpdateAsPaid(@Json(name = "memberId") val memberId: Int)