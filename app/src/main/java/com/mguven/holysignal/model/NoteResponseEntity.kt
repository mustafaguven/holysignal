package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class NoteResponseEntity(
    @Json(name = "Id") val Id: Int,
    @Json(name = "memberId") val memberId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "note") val note: String,
    @Json(name = "voteCount") val voteCount: Int,
    @Json(name = "date") val date: Long)