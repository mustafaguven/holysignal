package com.mguven.holysignal.model

import com.squareup.moshi.Json

data class TimePreference(
    @Json(name = "hourStart") val hourStart: Int,
    @Json(name = "hourFinish") val hourFinish: Int,
    @Json(name = "minuteStart") val minuteStart: Int,
    @Json(name = "minuteFinish") val minuteFinish: Int)