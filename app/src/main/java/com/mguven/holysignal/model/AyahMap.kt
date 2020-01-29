package com.mguven.holysignal.model

import com.mguven.holysignal.db.entity.SurahAyahSampleData


class AyahMap : LinkedHashMap<Int, SurahAyahSampleData?>() {

  fun getValue(i: Int): SurahAyahSampleData? {
    val entry: Map.Entry<Int, SurahAyahSampleData?> = getEntry(i) ?: return null
    return entry.value
  }

  fun getEntry(i: Int): Map.Entry<Int, SurahAyahSampleData?>? { // check if negetive index provided
    val entries: Set<Map.Entry<Int, SurahAyahSampleData?>> = entries
    var j = 0
    for (entry in entries) if (j++ == i) return entry
    return null
  }

}