package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.RemoveNoteResponseEntity
import com.squareup.moshi.Json

data class RemoveNoteEntity(
    @Json(name = "data") override val data: RemoveNoteResponseEntity,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface