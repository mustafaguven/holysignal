package com.mguven.holysignal.model.response

import com.mguven.holysignal.model.SignInResponseEntity
import com.squareup.moshi.Json

data class SignInEntity(
    @Json(name = "data") override val data: SignInResponseEntity? = null,
    override val message: String,
    override val status: Int
) : ResponseBaseEntityInterface