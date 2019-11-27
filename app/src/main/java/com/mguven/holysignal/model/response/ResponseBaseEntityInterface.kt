package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

interface ResponseBaseEntityInterface {
  @Json(name = "message") val message: String
  val data: Any?
  @Json(name = "status") val status: Int

}