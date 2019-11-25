package com.mguven.holysignal.viewmodel

import android.util.Log
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.SurahTranslateData
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

  suspend fun getEditionNameIdList() = database.editionDataDao().getNameIdList()

  suspend fun getDownloadableEditions() =
      database.editionDataDao().getDownloadableEditions()

  private fun downloadSurah(editionId: Int, textType: Int) {
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
        if(textType == ConstantVariables.TOP_TEXT) {
          cache.updateTopDownloadCount(surahResult.data?.surahNumber?.toInt())
        } else {
          cache.updateBottomDownloadCount(surahResult.data?.surahNumber?.toInt())
        }
        Log.e("AAA", "$editionId -- ${surahResult.data?.surahNumber}")
      }
    }
  }

  private fun downloadSurahTranslatedNames(editionId: Int, textType: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      val surahTranslateResult = surahApi.getSurahTranslationByLanguage(editionId)
      val languageId = surahTranslateResult.data?.languageId
      if(languageId != null) {
        surahTranslateResult.data.translationData.forEach {
          database.surahTranslateDataDao().deleteTranslatedNamesByLanguageIdAndSurahNumber(languageId, it.surahNumber)
          database.surahTranslateDataDao().insert(SurahTranslateData(0, it.surahNumber, languageId, it.name))
          if(textType == ConstantVariables.TOP_TEXT) {
            cache.updateTopDownloadSurahTranslateCount(surahTranslateResult.data.translationData.size, it.surahNumber)
          } else {
            cache.updateBottomDownloadSurahTranslateCount(surahTranslateResult.data.translationData.size, it.surahNumber)
          }
        }
      }
    }
  }

  fun download(editionId: Int, textType: Int) {
    downloadSurahTranslatedNames(editionId, textType)
    downloadSurah(editionId, textType)
  }


}
