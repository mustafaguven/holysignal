package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class MemberResponseEntity(
    @Json(name = "sessionno") val sessionNo: String)