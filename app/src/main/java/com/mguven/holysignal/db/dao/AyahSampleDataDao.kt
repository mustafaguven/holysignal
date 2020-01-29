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

//  @Query("SELECT Surah.number as surahNumber,  " +
//      " Surah.name as surahName, " +
//      " Surah.englishName as surahEnglishName, " +
//      " Surah.englishNameTranslation as surahEnglishNameTranslation, " +
//      " Surah.revelationType as surahRevelationType, " +
//      " Surah.StartingAyahNumber as startingAyahNumber, " +
//      " Surah.EndingAyahNumber as endingAyahNumber, " +
//      " Edition.language as language, " +
//      " AyahSample.Id as ayahId, " +
//      " AyahSample.number as ayahNumber, " +
//      " AyahSample.text as ayahText, " +
//      " AyahSample.numberInSurah as numberInSurah, " +
//      " AyahSample.juz as juz, " +
//      " AyahSample.noteId as noteId " +
//      " from AyahSample inner join Surah on Surah.number = AyahSample.surahNumber " +
//      " inner join Edition on Edition.Id = AyahSample.editionId " +
//      " WHERE AyahSample.editionId = :editionId AND AyahSample.number = :randomAyahNumber")
//  suspend fun getRandomAyah(editionId: Int, randomAyahNumber: Int): List<SurahAyahSampleData>

  @Query("Select " +
      " S.number as surahNumber, " +
      " S.name as surahName, " +
      " S.englishName as surahEnglishName, " +
      " S.englishNameTranslation as surahEnglishNameTranslation, " +
      " S.revelationType as surahRevelationType, " +
      " S.StartingAyahNumber as startingAyahNumber, " +
      " S.EndingAyahNumber as endingAyahNumber, " +
      " E.language as language, " +
      " A.Id as ayahId, " +
      " A.number as ayahNumber, " +
      " A.text as ayahText, " +
      " A.numberInSurah as numberInSurah, " +
      " A.juz as juz, " +
      " A.noteId as noteId, " +
      " Case WHEN T.name IS NULL THEN S.englishName else T.name end as surahNameByLanguage, " +
      " Case WHEN T.meaning IS NULL THEN S.englishNameTranslation else T.meaning end as meaning " +
      " From AyahSample A inner join Surah S on A.surahNumber = S.number " +
      " Inner Join Edition E on E.Id = A.editionId " +
      " inner join Language L on E.language = L.abbreviation " +
      " left join (SELECT languageId, name, meaning FROM SurahTranslate Where surahNumber = (Select A.surahNumber From AyahSample A WHERE A.number = :randomAyahNumber) ) T on L.Id = T.languageId " +
      " where editionId = :editionId " +
      " and A.number = :randomAyahNumber")
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