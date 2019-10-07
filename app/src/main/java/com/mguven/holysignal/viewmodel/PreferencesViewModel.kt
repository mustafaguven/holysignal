package com.mguven.holysignal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.MaxAyahCountData
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  fun getMaxAyahCount(): LiveData<MaxAyahCountData> {
    return database.ayahSampleDataDao().getMaxAyahCountByEditionId(cache.getTopTextEditionId())
  }

  fun getEditionNameIdList(): LiveData<List<EditionAdapterData>> {
    return database.editionDataDao().getNameIdList()
  }




}
