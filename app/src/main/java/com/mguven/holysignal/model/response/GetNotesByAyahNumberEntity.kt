package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.NoteResponseEntity
import com.squareup.moshi.Json

data class GetNotesByAyahNumberEntity(
    @Json(name = "data") override val data: NoteResponseEntity? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface