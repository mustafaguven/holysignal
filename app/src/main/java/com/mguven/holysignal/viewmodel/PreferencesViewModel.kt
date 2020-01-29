package com.mguven.holysignal.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.SurahTranslateData
import com.mguven.holysignal.exception.Exceptions
import com.mguven.holysignal.model.SetNewPasswordResponseEntity
import com.mguven.holysignal.model.SignInResponseEntity
import com.mguven.holysignal.model.request.*
import com.mguven.holysignal.model.response.GoogleUserProfileResponse
import com.mguven.holysignal.model.response.ResponseEntity
import com.mguven.holysignal.network.GoogleApi
import com.mguven.holysignal.network.MemberApi
import com.mguven.holysignal.network.PasswordRecoveryApi
import com.mguven.holysignal.network.SurahApi
import com.mguven.holysignal.util.DeviceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.TokenResponse
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val surahApi: SurahApi,
            private val memberApi: MemberApi,
            private val database: ApplicationDatabase,
            private val cache: ApplicationCache,
            private val deviceUtil: DeviceUtil,
            private val googleApi: GoogleApi,
            private val passwordRecoveryApi: PasswordRecoveryApi) : BaseViewModel() {

  val isMember = MutableLiveData<Int>()
  val memberShipData = MutableLiveData<ResponseEntity<SignInResponseEntity>>()
  val sendPasswordLiveData = MutableLiveData<ResponseEntity<Int>>()
  val setPasswordLiveData = MutableLiveData<ResponseEntity<SetNewPasswordResponseEntity>>()

  suspend fun getMaxAyahCount() =
      database.ayahSampleDataDao().getMaxAyahCountByEditionId(cache.getTopTextEditionId())

  suspend fun getEditionNameIdList(retrieveOnlyDownloaded: Boolean): List<EditionAdapterData> {
    val maxCount = if (retrieveOnlyDownloaded) ConstantVariables.MAX_AYAH_NUMBER else ConstantVariables.MAX_FREE_AYAH_NUMBER
    return database.editionDataDao().getNameIdList(maxCount)
  }


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
        isMember.postValue(response.status)
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
        if (response.status == ConstantVariables.RESPONSE_OK && response.data != null) {
          database.preferencesDataDao().updateUserInfo(response.data.memberId, response.data.name, response.data.surname)
          cache.updateToken(response.data.token)
          cache.updateMemberId(response.data.memberId)
        }
        memberShipData.postValue(response)
      }
    }
  }

  fun updateSessionNo(email: String, password: String) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = memberApi.updateSessionNo(RequestSignIn(email, password, cache.getUUID()))
        if (response.status == ConstantVariables.RESPONSE_OK && response.data != null) {
          database.preferencesDataDao().updateUserInfo(response.data.memberId, response.data.name, response.data.surname)
          cache.updateToken(response.data.token)
          cache.updateMemberId(response.data.memberId)
        }
        memberShipData.postValue(response)
      }
    }
  }

  fun signUp(name: String, surname: String, email: String, password: String) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = memberApi.save(RequestSignUp(name, surname, email, password, cache.getUUID()))
        if (response.status == ConstantVariables.RESPONSE_OK
            || (response.status == Exceptions.EMAIL_IS_WAITING_FOR_VERIFICATION)) {
          database.preferencesDataDao().updateUserInfo(response.data!!.memberId, name, surname)
          cache.updateToken(response.data.token)
          cache.updateMemberId(response.data.memberId)
        }
        memberShipData.postValue(response)
      }
    }
  }

  fun signInWithGoogle(tokenResponse: TokenResponse) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val userInfo = googleApi.getUserInfo(tokenResponse.accessToken!!)
        signInOrSignUp(userInfo)
      }
    }
  }

  private suspend fun signInOrSignUp(userInfo: GoogleUserProfileResponse) {
    val password = userInfo.email.hashCode().toString()
    val signInResponse = memberApi.signIn(RequestSignIn(userInfo.email, password, cache.getUUID()))
    if (signInResponse.status == ConstantVariables.RESPONSE_OK
        || (signInResponse.status == Exceptions.EMAIL_IS_WAITING_FOR_VERIFICATION)) {
      database.preferencesDataDao().updateUserInfo(signInResponse.data!!.memberId, userInfo.given_name, userInfo.family_name)
      cache.updateToken(signInResponse.data.token)
      cache.updateMemberId(signInResponse.data.memberId)
    } else if (signInResponse.status == Exceptions.SESSION_IS_DIFFERENT || signInResponse.status == Exceptions.STORED_PHONE_IS_CHANGED) {
      updateSessionNo(userInfo.email, password)
    } else if (signInResponse.status == Exceptions.STORED_PHONE_CHANGED_MORE_THAN_TWO_TIMES_AND_IS_UNUSABLE_NOW) {
      memberShipData.postValue(signInResponse)
    } else {
      signUp(userInfo.given_name, userInfo.family_name, userInfo.email, password)
    }
  }

  fun signInWithFacebook(email: String, firstName: String, lastName: String) {
    CoroutineScope(Dispatchers.IO).launch {
      Log.e("signInWithFacebook", "$email --- $firstName --- $lastName")
      val password = firstName.plus(lastName).hashCode().toString()
      val signInResponse = memberApi.signIn(RequestSignIn(email, password, cache.getUUID()))
      if (signInResponse.status == ConstantVariables.RESPONSE_OK
          || (signInResponse.status == Exceptions.EMAIL_IS_WAITING_FOR_VERIFICATION)) {
        database.preferencesDataDao().updateUserInfo(signInResponse.data!!.memberId, firstName, lastName)
        cache.updateToken(signInResponse.data.token)
        cache.updateMemberId(signInResponse.data.memberId)
      } else if (signInResponse.status == Exceptions.SESSION_IS_DIFFERENT || signInResponse.status == Exceptions.STORED_PHONE_IS_CHANGED) {
        updateSessionNo(email, password)
      } else if (signInResponse.status == Exceptions.STORED_PHONE_CHANGED_MORE_THAN_TWO_TIMES_AND_IS_UNUSABLE_NOW) {
        memberShipData.postValue(signInResponse)
      } else {
        signUp(firstName, lastName, email, password)
      }
    }
  }

  fun sendPasswordRecoveryMail(email: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val validationNumber= passwordRecoveryApi.reset(RequestPasswordReset(email))
      sendPasswordLiveData.postValue(validationNumber)
    }
  }

  fun setNewPassword(email: String, newPassword: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val result= passwordRecoveryApi.setNewPassword(RequestSetNewPassword(email, newPassword))
      setPasswordLiveData.postValue(result)
    }
  }


}
