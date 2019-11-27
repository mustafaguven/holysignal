package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.MemberResponseEntity
import com.squareup.moshi.Json

data class MemberEntity(
    @Json(name = "data") override val data: MemberResponseEntity? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface