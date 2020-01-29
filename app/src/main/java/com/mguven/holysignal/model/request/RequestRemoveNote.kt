package com.mguven.holysignal.model.request

import com.squareup.moshi.Json

data class RequestRemoveNote(@Json(name = "ayahNoteId") val ayahNoteId: Int)