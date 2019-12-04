package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class MyVoteEntity(
    @Json(name = "ayahNoteId") val ayahNoteId: Int,
    @Json(name = "vote") val vote: Int)