package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.MaxAyahCountData
import com.mguven.holysignal.db.entity.SurahAyahSampleData

@Dao
interface AyahSampleDataDao {

  @Query("SELECT * from AyahSample WHERE editionId = :editionId")
  fun getAll(editionId: Int): LiveData<List<AyahSampleData>>

  @Insert(onConflict = REPLACE)
  fun insert(ayahData: AyahSampleData)

  @Query("DELETE from AyahSample")
  fun deleteAll()

  @Query("SELECT Surah.number as surahNumber,  " +
      " Surah.name as surahName, " +
      " Surah.englishName as surahEnglishName, " +
      " Surah.englishNameTranslation as surahEnglishNameTranslation, " +
      " Surah.revelationType as surahRevelationType, " +
      " Surah.StartingAyahNumber as startingAyahNumber, " +
      " Surah.EndingAyahNumber as endingAyahNumber, " +
      " Edition.language as language, " +
      " AyahSample.Id as ayahId, " +
      " AyahSample.number as ayahNumber, " +
      " AyahSample.text as ayahText, " +
      " AyahSample.numberInSurah as numberInSurah, " +
      " AyahSample.juz as juz, " +
      " AyahSample.noteId as noteId " +
      " from AyahSample inner join Surah on Surah.number = AyahSample.surahNumber " +
      " inner join Edition on Edition.Id = AyahSample.editionId " +
      " WHERE AyahSample.editionId = :editionId AND AyahSample.number = :randomAyahNumber")
  suspend fun getRandomAyah(editionId: Int, randomAyahNumber: Int): List<SurahAyahSampleData>

  @Query(" SELECT max(number) as max from AyahSample WHERE editionId = :editionId ")
  suspend fun getMaxAyahCountByEditionId(editionId: Int): MaxAyahCountData

  @Query("UPDATE AyahSample SET noteId = :noteId WHERE Id = :ayahId")
  suspend fun updateNoteId(ayahId: Int, noteId: Int)

  @RawQuery
  suspend fun getAyahsByKeyword(query: SupportSQLiteQuery): List<Int>

  @Query("DELETE FROM AyahSample WHERE editionId = :editionId")
  suspend fun deleteSurahsByEditionId(editionId: Int)
}