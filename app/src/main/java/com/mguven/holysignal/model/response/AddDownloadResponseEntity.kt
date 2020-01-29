package com.mguven.holysignal.model.response

import com.squareup.moshi.Json

data class AddDownloadResponseEntity(
    @Json(name = "downloadId") val downloadId: Int)