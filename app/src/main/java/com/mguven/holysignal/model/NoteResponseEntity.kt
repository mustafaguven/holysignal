package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class NoteResponseEntity(
    @Json(name = "notes") val notes: List<NoteEntity>,
    @Json(name = "myVotes") val myVotes: List<MyVoteEntity>)