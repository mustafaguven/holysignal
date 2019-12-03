package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.InsertVoterResponseEntity
import com.squareup.moshi.Json

data class InsertVoterEntity(
    @Json(name = "data") override val data: InsertVoterResponseEntity,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface