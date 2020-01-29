package com.mguven.holysignal.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.SurahTranslateData
import com.mguven.holysignal.model.SignInResponseEntity
import com.mguven.holysignal.model.request.RequestAddDownload
import com.mguven.holysignal.model.request.RequestAddOrder
import com.mguven.holysignal.model.request.RequestUpdateAsPaid
import com.mguven.holysignal.model.response.ResponseEntity
import com.mguven.holysignal.network.DownloadApi
import com.mguven.holysignal.network.MemberApi
import com.mguven.holysignal.network.OrderApi
import com.mguven.holysignal.network.SurahApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class DownloadViewModel @Inject
constructor(private val surahApi: SurahApi,
            private val orderApi: OrderApi,
            private val downloadApi: DownloadApi,
            private val memberApi: MemberApi,
            private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  val isMember = MutableLiveData<Int>()
  val paidUserLiveData = MutableLiveData<ResponseEntity<Int>>()
  val updateUserAsPaidLiveData = MutableLiveData<ResponseEntity<String>>()
  val memberShipData = MutableLiveData<ResponseEntity<SignInResponseEntity>>()

  suspend fun getDownloadableEditions() =
      database.editionDataDao().getDownloadableEditions()

  suspend fun getAllLanguages() = database.languageDataDao().getAll()

  suspend fun getBooksByTheSelectedLanguage(languageId: Int) = database.editionDataDao().getEditionsBySelectedLanguage(languageId)

  fun download(editionId: Int) {
    downloadSurahTranslatedNames(editionId)
    downloadSurah(editionId)
  }

  private fun downloadSurah(editionId: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
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
          cache.updateTopDownloadCount(surahResult.data?.surahNumber?.toInt())
          Log.e("AAA", "$editionId -- ${surahResult.data?.surahNumber}")
        }
      } catch (ex: Exception) {
        isMember.postValue(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
      }
    }
  }

  private fun downloadSurahTranslatedNames(editionId: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val surahTranslateResult = surahApi.getSurahTranslationByLanguage(editionId)
        val languageId = surahTranslateResult.data?.languageId
        if (languageId != null) {
          surahTranslateResult.data.translationData.forEach {
            database.surahTranslateDataDao().deleteTranslatedNamesByLanguageIdAndSurahNumber(languageId, it.surahNumber)
            database.surahTranslateDataDao().insert(SurahTranslateData(0, it.surahNumber, languageId, it.name, it.meaning, it.specification))
            cache.updateTopDownloadSurahTranslateCount(surahTranslateResult.data.translationData.size, it.surahNumber)
          }
        }
      } catch (ex: Exception) {
        isMember.postValue(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
      }
    }
  }

  fun addOrder() {
    CoroutineScope(Dispatchers.IO).launch {
      val result = orderApi.addOrder(RequestAddOrder(cache.getMemberId()))
      //addDownload(result.data!!.orderId)
    }
  }

  fun addDownload(editionId: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      val result = downloadApi.addDownload(RequestAddDownload(cache.getMemberId(), editionId))
      result.data!!.downloadId
    }
  }

  fun checkIsPaidUser() {
    CoroutineScope(Dispatchers.IO).launch {
      val result = memberApi.isPaidUser(cache.getMemberId())
      paidUserLiveData.postValue(result)
    }
  }

  fun setMemberAsPaid() {
    CoroutineScope(Dispatchers.IO).launch {
      val result = memberApi.updateAsPaid(RequestUpdateAsPaid(cache.getMemberId()))
      updateUserAsPaidLiveData.postValue(result)
    }
  }

}
