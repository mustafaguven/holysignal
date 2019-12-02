package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.db.entity.SurahData

@Dao
interface SurahDataDao {

  @Query("Select Case WHEN Sub.name IS NULL THEN Sub.englishName else Sub.name end as 'key', A.surahNumber as value, Sub.startingAyahNumber as 'min', Sub.endingAyahNumber as 'max' from AyahSample A Inner Join ( " +
      " Select  S.number, S.englishName, T.name, S.startingAyahNumber, S.endingAyahNumber from Surah S " +
      "       LEft join SurahTranslate T on S.number = T.surahNumber " +
      "   and languageId = (Select L.Id from Language L Inner Join Edition E on L.abbreviation = E.language Where E.Id = :editionId) " +
      "       group by S.englishName " +
      "       order by S.number " +
      " ) as Sub on Sub.number = A.surahNumber " +
      " group by A.surahNumber " +
      " order by A.surahNumber ")
  suspend fun getAvailableSurahListByEditionId(editionId: Int): List<AvailableSurahItem>

  @Query("SELECT * from Surah")
  suspend fun getAll(): List<SurahData>

  @Insert(onConflict = REPLACE)
  suspend fun insert(surahData: SurahData)

  @Query("DELETE from Surah")
  suspend fun deleteAll()


}