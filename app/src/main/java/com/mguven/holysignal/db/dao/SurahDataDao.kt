package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.db.entity.SurahData

@Dao
interface SurahDataDao {

  @Query("SELECT * from Surah")
  suspend fun getAll(): List<SurahData>

  @Query("SELECT '(' || S.number || ')'  || ' ' ||  S.englishName as 'key', S.number as value, S.startingAyahNumber as 'min', S.endingAyahNumber as 'max' " +
      " FROM Surah S INNER JOIN AyahSample A on S.number = A.surahNumber " +
      " WHERE editionId = :editionId " +
      " group by S.englishName " +
      " order by S.Id ")
  suspend fun getAvailableSurahListByEditionId(editionId: Int): List<AvailableSurahItem>

  @Insert(onConflict = REPLACE)
  suspend fun insert(surahData: SurahData)

  @Query("DELETE from Surah")
  suspend fun deleteAll()



}