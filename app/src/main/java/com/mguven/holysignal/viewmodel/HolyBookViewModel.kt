package com.mguven.holysignal.viewmodel

import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import javax.inject.Inject


class HolyBookViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  fun getAyahTopText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getTopTextEditionId(), randomAyahNumber)

  fun getAyahBottomText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah2(cache.getBottomTextEditionId(), randomAyahNumber)

  fun getAyahList() =
      database.ayahSampleDataDao().getAll(53)

/*  fun getSelectedSurah(ayahId: Int) = database.ayahSampleDataDao().getAyahBottomText(ayahId)*/

  fun getSurahList() =
      database.surahDataDao().getAll()

  fun getEditionList() = database.editionDataDao().getAll()

}
