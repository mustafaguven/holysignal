package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

data class AddOrderResponseEntity(
    @Json(name = "orderId") val orderId: Int)