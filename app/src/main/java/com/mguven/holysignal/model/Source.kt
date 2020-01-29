package com.mguven.holysignal.model

import com.squareup.moshi.Json
import java.io.Serializable

data class Source(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null) : Serializable