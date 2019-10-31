package com.mguven.holysignal.cache

import android.content.SharedPreferences
import com.google.gson.Gson
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.model.AyahSearchResult


class ApplicationCache(private val applicationSharedPreferences: SharedPreferences,
                       private val gson: Gson) {

  private fun <T> getObjectWithGenericDeserializer(key: String, clz: Class<T>): T {
    return gson.fromJson(applicationSharedPreferences.getString(key, null), clz)
  }

  private fun <T> setObjectWithGenericSerializer(key: String, t: T) {
    this.applicationSharedPreferences.edit().putString(key, gson.toJson(t)).apply()
  }

  fun removeApplicationCache() {
    applicationSharedPreferences.edit().clear().apply()
  }

  fun updateTopTextEditionId(editionId: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.TOP_TEXT_EDITION_ID, editionId).apply()
  }

  fun getTopTextEditionId() =
      this.applicationSharedPreferences.getInt(CacheKey.TOP_TEXT_EDITION_ID, 11)

  fun updateBottomTextEditionId(editionId: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.BOTTOM_TEXT_EDITION_ID, editionId).apply()
  }

  fun getBottomTextEditionId() =
      this.applicationSharedPreferences.getInt(CacheKey.BOTTOM_TEXT_EDITION_ID, 53)

  fun updateLastShownAyah(lastShownAyah: SurahAyahSampleData?) {
    setObjectWithGenericSerializer(CacheKey.LAST_SHOWN_AYAH, lastShownAyah)
    whenNotNull(lastShownAyah) {
      updateLastShownAyahNumber(it.ayahNumber)
    }
  }

  fun getLastShownAyah(): SurahAyahSampleData? = getObjectWithGenericDeserializer(CacheKey.LAST_SHOWN_AYAH,
      SurahAyahSampleData::class.java)

  fun getPlaymode(): Int {
    return this.applicationSharedPreferences.getInt(CacheKey.PLAY_MODE, 0)
  }

  fun updatePlaymode(playmode: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.PLAY_MODE, playmode).apply()
  }

  fun updateLastShownAyahNumber(ayahNumber: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.LAST_SHOWN_AYAH_NUMBER, ayahNumber).apply()
  }

  fun getLastShownAyahNumber() =
      this.applicationSharedPreferences.getInt(CacheKey.LAST_SHOWN_AYAH_NUMBER, 1)

  fun updateMaxAyahCount(maxAyahCount: Int) =
      this.applicationSharedPreferences.edit().putInt(CacheKey.MAX_AYAH_COUNT, maxAyahCount).apply()

  fun getMaxAyahCount() =
      this.applicationSharedPreferences.getInt(CacheKey.MAX_AYAH_COUNT, 50)

/*  fun updateSearchKeyword(words: String) =
      this.applicationSharedPreferences.edit().putString(CacheKey.SEARCH_KEYWORD, words).apply()

  fun getSearchKeyword() =
      this.applicationSharedPreferences.getString(CacheKey.SEARCH_KEYWORD, "")*/

  fun updateAyahSearchResult(obj: AyahSearchResult?) {
    setObjectWithGenericSerializer(CacheKey.AYAH_SEARCH_RESULT, obj)
  }

  fun getAyahSearchResult(): AyahSearchResult? = getObjectWithGenericDeserializer(CacheKey.AYAH_SEARCH_RESULT,
      AyahSearchResult::class.java)

/*  fun updateUserInfo(userInformation: UserInformation?) {
    setObjectWithGenericSerializer(CacheKey.USER_INFO, userInformation)
  }

  fun getUserInfo(): UserInformation? = getObjectWithGenericDeserializer(CacheKey.USER_INFO,
      UserInformation::class.java)*/

}
