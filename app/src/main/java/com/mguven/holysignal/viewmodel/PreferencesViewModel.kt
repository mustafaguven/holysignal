package com.mguven.holysignal.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.SurahTranslateData
import com.mguven.holysignal.model.request.RequestMemberSession
import com.mguven.holysignal.model.request.RequestSignIn
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.network.MemberApi
import com.mguven.holysignal.network.SurahApi
import com.mguven.holysignal.util.DeviceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val surahApi: SurahApi,
            private val memberApi: MemberApi,
            private val database: ApplicationDatabase,
            private val cache: ApplicationCache,
            private val deviceUtil: DeviceUtil) : BaseViewModel() {


  val isMember = MutableLiveData<Int>()
  val memberShipData = MutableLiveData<SignInEntity>()

  suspend fun getMaxAyahCount() =
      database.ayahSampleDataDao().getMaxAyahCountByEditionId(cache.getTopTextEditionId())

  suspend fun getEditionNameIdList() = database.editionDataDao().getNameIdList()

  suspend fun getDownloadableEditions() =
      database.editionDataDao().getDownloadableEditions()

  private fun downloadSurah(editionId: Int, textType: Int) {
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
          if (textType == ConstantVariables.TOP_TEXT) {
            cache.updateTopDownloadCount(surahResult.data?.surahNumber?.toInt())
          } else {
            cache.updateBottomDownloadCount(surahResult.data?.surahNumber?.toInt())
          }
          Log.e("AAA", "$editionId -- ${surahResult.data?.surahNumber}")
        }
      } catch (ex: Exception) {
        isMember.postValue(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
      }
    }
  }

  private fun downloadSurahTranslatedNames(editionId: Int, textType: Int) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val surahTranslateResult = surahApi.getSurahTranslationByLanguage(editionId)
        val languageId = surahTranslateResult.data?.languageId
        if (languageId != null) {
          surahTranslateResult.data.translationData.forEach {
            database.surahTranslateDataDao().deleteTranslatedNamesByLanguageIdAndSurahNumber(languageId, it.surahNumber)
            database.surahTranslateDataDao().insert(SurahTranslateData(0, it.surahNumber, languageId, it.name, it.meaning, it.specification))
            if (textType == ConstantVariables.TOP_TEXT) {
              cache.updateTopDownloadSurahTranslateCount(surahTranslateResult.data.translationData.size, it.surahNumber)
            } else {
              cache.updateBottomDownloadSurahTranslateCount(surahTranslateResult.data.translationData.size, it.surahNumber)
            }
          }
        }
      } catch (ex: Exception) {
        isMember.postValue(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
      }
    }
  }

  fun download(editionId: Int, textType: Int) {
    downloadSurahTranslatedNames(editionId, textType)
    downloadSurah(editionId, textType)
  }

  fun loginCheck() {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val token = cache.getToken()
        val response = memberApi.getMemberSessionNo(RequestMemberSession(token))
        isMember.postValue(if (response.status == 1) ConstantVariables.MEMBER_IS_FOUND else ConstantVariables.MEMBER_IS_NOT_FOUND)
      }
    } else {
      isMember.postValue(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
    }
  }

  suspend fun getMemberInfo() = database.preferencesDataDao().getAll()

  fun signIn(email: String, password: String) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = memberApi.signIn(RequestSignIn(email, password, cache.getUUID()))
        if (response.status == 1) {
          cache.updateToken(response.data!!.token)
        }
        memberShipData.postValue(response)
      }
    }
  }

  fun updateSessionNo(email: String, password: String) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = memberApi.updateSessionNo(RequestSignIn(email, password, cache.getUUID()))
        if (response.status == 1) {
          cache.updateToken(response.data!!.token)
        }
        memberShipData.postValue(response)
      }
    }
  }


}
