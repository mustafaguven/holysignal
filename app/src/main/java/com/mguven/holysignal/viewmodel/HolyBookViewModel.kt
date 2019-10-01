package com.mguven.holysignal.viewmodel

import com.mguven.holysignal.db.ApplicationDatabase
import javax.inject.Inject


class HolyBookViewModel @Inject
constructor(private val database: ApplicationDatabase) : BaseViewModel() {

  fun getRandomAyah(editionId: Int, randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(editionId, randomAyahNumber)

  fun getAyahList() =
      database.ayahSampleDataDao().getAll(53)

  fun getSurahList() =
      database.surahDataDao().getAll()

  fun getEditionList() = database.editionDataDao().getAll()

}
