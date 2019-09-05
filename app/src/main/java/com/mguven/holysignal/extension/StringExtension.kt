package com.mguven.holysignal.extension

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import kotlin.math.absoluteValue

fun String.hoursAgo(): String {
  //2018-10-11T17:34:00Z
  //2018-10-11T14:40:08.8578193Z
  // java.lang.IllegalArgumentException: Invalid format: "2018-10-11T14:40:08.8578193Z" is malformed at ".8578193Z"
  //todo: fix this
  try {
    val duration = Duration(this.parseDateTime(), DateTime.now())
    if (duration.standardDays.absoluteValue == 0L) {
      if (duration.standardMinutes.absoluteValue <= 59) {
        return "${duration.standardMinutes.absoluteValue} mins ago"
      } else if (duration.standardHours.absoluteValue in 1..24) {
        return "${duration.standardHours.absoluteValue} hours ago"
      }
    } else {
      return "${duration.standardDays.absoluteValue} days ago"
    }
    return this
  } catch (ex: Exception) {
    return this
  }
}

fun String.parseDateTime(): DateTime {
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
  return formatter.parseDateTime(substring(0, 19))
}