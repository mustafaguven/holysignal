package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestDownload(@Json(name = "memberId") val memberId: Int,
                                @Json(name = "orderId") val orderId: Int)