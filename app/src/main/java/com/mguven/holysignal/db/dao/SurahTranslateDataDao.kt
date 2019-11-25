package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.db.entity.SurahTranslateData

@Dao
interface SurahTranslateDataDao {

  @Query("SELECT * from SurahTranslate")
  suspend fun getAll(): List<SurahTranslateData>

  @Query("DELETE from SurahTranslate WHERE languageId = :languageId")
  suspend fun deleteTranslatedNamesByEditionId(languageId: Int)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(surahTranslateData: SurahTranslateData) : Long

  @Query("DELETE from SurahTranslate WHERE surahNumber = :surahNumber AND languageId = :languageId")
  suspend fun deleteTranslatedNamesByLanguageIdAndSurahNumber(languageId: Int, surahNumber: Int)

}