package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.AddNoteResponseEntity
import com.squareup.moshi.Json

data class AddNoteEntity(
    @Json(name = "data") override val data: AddNoteResponseEntity,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface