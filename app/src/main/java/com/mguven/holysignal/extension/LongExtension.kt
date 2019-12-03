package com.mguven.holysignal.extension

import org.joda.time.format.DateTimeFormat

fun Long.parseDateToString(): String {
  val formatter = DateTimeFormat.forPattern("yyyyMMddHHmm")
  return formatter.parseDateTime(this.toString()).toString("dd.MM.yyyy HH:mm")
}