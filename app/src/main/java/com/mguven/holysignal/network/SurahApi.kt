package com.mguven.holysignal.network


import com.mguven.holysignal.model.SurahResponseEntity
import com.mguven.holysignal.model.SurahTranslateEntity
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface SurahApi {

  @GET("surah")
  suspend fun getSurahByEditionId(@Query("surahNumber") surahNumber: Int,
                                  @Query("editionId") editionId: Int): ResponseEntity<SurahResponseEntity>

  @GET("surahtranslationbylanguage")
  suspend fun getSurahTranslationByLanguage(@Query("editionId") editionId: Int): ResponseEntity<SurahTranslateEntity>

}
