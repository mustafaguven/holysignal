package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.db.entity.SurahData

@Dao
interface SurahDataDao {

  @Query("SELECT * from Surah")
  fun getAll(): LiveData<List<SurahData>>

  @Query("SELECT S.englishName as 'key', S.number as value, min(A.numberInSurah) as 'min', max(A.numberInSurah) as 'max' " +
      " FROM Surah S INNER JOIN AyahSample A on S.number = A.surahNumber " +
      " WHERE editionId = :editionId " +
      " group by S.englishName " +
      " order by S.Id ")
  fun getAvailableSurahListByEditionId(editionId: Int): LiveData<List<AvailableSurahItem>>

  @Insert(onConflict = REPLACE)
  fun insert(surahData: SurahData)

  @Query("DELETE from Surah")
  fun deleteAll()



}