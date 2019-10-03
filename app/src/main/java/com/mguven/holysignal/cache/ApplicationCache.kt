package com.mguven.holysignal.cache

import android.content.SharedPreferences
import com.google.gson.Gson


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

/*  fun updateUserInfo(userInformation: UserInformation?) {
    setObjectWithGenericSerializer(CacheKey.USER_INFO, userInformation)
  }

  fun getUserInfo(): UserInformation? = getObjectWithGenericDeserializer(CacheKey.USER_INFO,
      UserInformation::class.java)*/

}
