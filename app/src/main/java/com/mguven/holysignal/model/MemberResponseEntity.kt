package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class MemberResponseEntity(
    @Json(name = "sessionNo") val sessionNo: String)