package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class RemoveNoteResponseEntity(
    @Json(name = "action") val action: String)