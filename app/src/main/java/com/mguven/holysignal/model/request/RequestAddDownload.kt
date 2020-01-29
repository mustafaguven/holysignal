package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestAddDownload(@Json(name = "memberId") val memberId: Int,
                                @Json(name = "editionId") val editionId: Int)