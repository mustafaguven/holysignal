package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class AddNoteResponseEntity(
    @Json(name = "action") val action: String)