package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class InsertVoterResponseEntity(
    @Json(name = "action") val action: String)