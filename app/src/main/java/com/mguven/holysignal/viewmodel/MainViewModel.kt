package com.mguven.holysignal.viewmodel

import com.mguven.holysignal.db.ApplicationDatabase
import javax.inject.Inject


class MainViewModel @Inject
constructor(private val database: ApplicationDatabase) : BaseViewModel() {

  fun getAyahList() =
      database.ayahSampleDataDao().getAll(53)

  fun getSurahList() =
      database.surahDataDao().getAll()

  fun getEditionList() = database.editionDataDao().getAll()

}
