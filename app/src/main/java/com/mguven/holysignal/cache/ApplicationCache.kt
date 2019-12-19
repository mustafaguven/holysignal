package com.mguven.holysignal.cache

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.model.AyahSearchResult
import com.mguven.holysignal.model.FavouriteAyahList
import java.util.*


class ApplicationCache(private val applicationSharedPreferences: SharedPreferences,
                       private val gson: Gson) {

  val downloadedTopSurah = MutableLiveData<Int>()
  val downloadedTopSurahTranslate = MutableLiveData<IntArray>()

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
      this.applicationSharedPreferences.getInt(CacheKey.TOP_TEXT_EDITION_ID, 77)

  fun updateBottomTextEditionId(editionId: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.BOTTOM_TEXT_EDITION_ID, editionId).apply()
  }

  fun getBottomTextEditionId() =
      this.applicationSharedPreferences.getInt(CacheKey.BOTTOM_TEXT_EDITION_ID, 60)

  fun updateLastShownAyah(lastShownAyah: SurahAyahSampleData?) {
    surahAyahSampleData = lastShownAyah
    setObjectWithGenericSerializer(CacheKey.LAST_SHOWN_AYAH, lastShownAyah)
    whenNotNull(lastShownAyah) {
      updateLastShownAyahNumber(it.ayahNumber)
    }
  }

  private var surahAyahSampleData: SurahAyahSampleData? = null
  fun getLastShownAyah(): SurahAyahSampleData? {
    if (surahAyahSampleData == null) {
      surahAyahSampleData = getObjectWithGenericDeserializer(CacheKey.LAST_SHOWN_AYAH,
          SurahAyahSampleData::class.java)
    }
    return surahAyahSampleData
  }

  fun getPlaymode(): Int {
    return this.applicationSharedPreferences.getInt(CacheKey.PLAY_MODE, 0)
  }

  fun updatePlaymode(playmode: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.PLAY_MODE, playmode).apply()
  }

  private var lastShownAyahNumber = Int.MIN_VALUE
  fun updateLastShownAyahNumber(ayahNumber: Int) {
    this.applicationSharedPreferences.edit().putInt(CacheKey.LAST_SHOWN_AYAH_NUMBER, ayahNumber).apply()
    lastShownAyahNumber = ayahNumber
  }

  fun getLastShownAyahNumber(): Int {
    if (lastShownAyahNumber == Int.MIN_VALUE) {
      lastShownAyahNumber = this.applicationSharedPreferences.getInt(CacheKey.LAST_SHOWN_AYAH_NUMBER, 1)
    }
    return lastShownAyahNumber
  }


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

  fun updateFavouriteAyahs(obj: FavouriteAyahList?) {
    setObjectWithGenericSerializer(CacheKey.FAVOURITE_AYAH_LIST, obj)
  }

  fun getFavouriteAyahs(): FavouriteAyahList? = getObjectWithGenericDeserializer(CacheKey.FAVOURITE_AYAH_LIST,
      FavouriteAyahList::class.java)

  private var lastShownFavouriteIndex = Int.MIN_VALUE
  fun updateLastShownFavouriteIndex(randomIndex: Int) {
    lastShownFavouriteIndex = randomIndex
    this.applicationSharedPreferences.edit().putInt(CacheKey.LAST_SHOWN_FAVOURITE_INDEX, randomIndex).apply()
  }

  fun getLatestShownFavouriteIndex(): Int {
    if (lastShownFavouriteIndex == Int.MIN_VALUE) {
      lastShownFavouriteIndex = this.applicationSharedPreferences.getInt(CacheKey.LAST_SHOWN_FAVOURITE_INDEX, 0)
    }
    return lastShownFavouriteIndex
  }

  fun updateTopDownloadCount(surahNumber: Int?) {
    downloadedTopSurah.postValue(surahNumber)
  }

  fun updateTopDownloadSurahTranslateCount(total: Int, surahNumber: Int) {
    downloadedTopSurahTranslate.postValue(intArrayOf(total, surahNumber))
  }

  fun getToken() = this.applicationSharedPreferences.getString(CacheKey.TOKEN, "token")

  fun updateToken(token: String) = this.applicationSharedPreferences.edit().putString(CacheKey.TOKEN, token).apply()

  fun getUUID() = this.applicationSharedPreferences.getString(CacheKey.UUID, UUID.randomUUID().toString())

  fun setUUID(uuid: String) = this.applicationSharedPreferences.edit().putString(CacheKey.UUID, uuid).apply()

  fun updateUUIDIfNeeded() {
    if (getUUID().isNullOrEmpty()) {
      setUUID(UUID.randomUUID().toString())
    }
  }

  fun isActive() = this.applicationSharedPreferences.getBoolean(CacheKey.ACTIVE_PASSIVE, true)

  fun updateActivePassive(activePassive: Boolean) =
      this.applicationSharedPreferences.edit().putBoolean(CacheKey.ACTIVE_PASSIVE, activePassive).apply()

  fun hasSecondLanguageSupport() = this.applicationSharedPreferences.getBoolean(CacheKey.SECOND_LANGUAGE_SUPPORT, false)

  fun updateSecondLanguageSupport(hasSupport: Boolean) =
      this.applicationSharedPreferences.edit().putBoolean(CacheKey.SECOND_LANGUAGE_SUPPORT, hasSupport).apply()

  fun getBookmark() = this.applicationSharedPreferences.getInt(CacheKey.BOOKMARK, -1)

  fun updateBookmark(ayahNumber: Int) =
      this.applicationSharedPreferences.edit().putInt(CacheKey.BOOKMARK, ayahNumber).apply()

  fun getMemberId() = this.applicationSharedPreferences.getInt(CacheKey.MEMBER_ID, -1)

  fun updateMemberId(memberId: Int) = this.applicationSharedPreferences.edit().putInt(CacheKey.MEMBER_ID, memberId).apply()

  fun arrangeAyahCacheForOpening() {
    if (getLastShownAyahNumber() != Int.MIN_VALUE) {
      updateLastShownAyahNumber(getLastShownAyahNumber() + 1)
    }
  }

  fun clear() {
    this.applicationSharedPreferences.edit().clear().apply()
  }


//  fun getAutoModeLevel(): Int {
//    if (autoModeLevel < 0) {
//      autoModeLevel = this.applicationSharedPreferences.getInt(CacheKey.AUTO_MODE_LEVEL, 0)
//    }
//    return autoModeLevel
//  }
//
//  fun updateAutoModeLevel(autoModeLevel: Int) {
//    this.autoModeLevel = autoModeLevel
//    this.applicationSharedPreferences.edit().putInt(CacheKey.AUTO_MODE_LEVEL, autoModeLevel).apply()
//  }

}
