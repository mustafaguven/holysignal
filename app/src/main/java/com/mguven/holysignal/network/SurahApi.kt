package com.mguven.holysignal.network


import com.mguven.holysignal.model.SurahEntity

import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface SurahApi {

  @GET("surah")
  suspend fun getSurahByEditionId(@Query("surahNumber") surahNumber: Int,
              @Query("editionId") editionId: Int): SurahEntity

}
