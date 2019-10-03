package com.mguven.holysignal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.PreferencesData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  lateinit var preferencesLiveData: LiveData<List<PreferencesData>>

  fun getPreferences(): LiveData<List<PreferencesData>> {
    preferencesLiveData = database.preferencesDataDao().getAll()
    preferencesLiveData.observeForever(observer)
    return preferencesLiveData
  }

  private val observer = Observer<List<PreferencesData>> {
    cache.updateTopTextEditionId(it[0].topTextEditionId)
    cache.updateBottomTextEditionId(it[0].bottomTextEditionId)
  }

  override fun onCleared() {
    super.onCleared()
    preferencesLiveData.removeObserver(observer)
  }

  fun updateSelectedEditionId(topTextEditionId: Int, bottomTextEditionId: Int): Job = runBlocking {
    launch(Dispatchers.Default){
      database.preferencesDataDao().upsertEditionId(topTextEditionId, bottomTextEditionId)
    }
  }

}
