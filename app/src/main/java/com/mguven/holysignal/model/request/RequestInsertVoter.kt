package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestInsertVoter(@Json(name = "memberId") val memberId: Int,
                              @Json(name = "ayahNoteId") val ayahNoteId: Int,
                              @Json(name = "vote") val vote: Int)