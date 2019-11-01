package com.mguven.holysignal.extension

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import kotlin.math.absoluteValue

fun String.hoursAgo(): String {
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

fun String?.removeBoxBrackets() = this?.toLowerCase()?.replace("[", "")
    ?.replace("]", "")
    ?.replace(" ", "")
    ?: ""

fun String?.removeBoxBracketsAndPutSpaceAfterComma() = this?.removeBoxBrackets()
    ?.replace(",", ", ")
    ?: ""