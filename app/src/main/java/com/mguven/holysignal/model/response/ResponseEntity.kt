package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

data class ResponseEntity<T>(
    @Json(name = "data") override val data: T? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface