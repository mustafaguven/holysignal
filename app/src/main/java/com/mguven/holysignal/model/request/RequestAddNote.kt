package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestAddNote(@Json(name = "memberId") val memberId: Int,
                          @Json(name = "ayahNumber") val ayahNumber: Int,
                          @Json(name = "note") val note: String)