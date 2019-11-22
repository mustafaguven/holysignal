package com.mguven.holysignal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.network.SurahApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val surahApi: SurahApi,
            private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  suspend fun getMaxAyahCount() =
      database.ayahSampleDataDao().getMaxAyahCountByEditionId(cache.getTopTextEditionId())

  fun getEditionNameIdList(): LiveData<List<EditionAdapterData>> {
    return database.editionDataDao().getNameIdList()
  }

  suspend fun getDownloadableEditions() =
      database.editionDataDao().getDownloadableEditions()

  fun downloadSurah(editionId: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      database.ayahSampleDataDao().deleteSurahsByEditionId(editionId)
      for (surahNumber in ConstantVariables.MIN_SURAH_NUMBER..ConstantVariables.MAX_SURAH_NUMBER) {
        val surahResult = surahApi.getSurahByEditionId(surahNumber, editionId)
        surahResult.data?.ayahs?.forEach {
          database.ayahSampleDataDao().insert(AyahSampleData(0,
              editionId,
              surahNumber,
              it.number,
              it.text,
              it.numberInSurah,
              it.juz,
              null
          ))
        }
        cache.updateDownloadCount(surahResult.data?.surahNumber?.toInt())
        Log.e("AAA", "$editionId -- ${surahResult.data?.surahNumber}")
      }
    }

  }


}
