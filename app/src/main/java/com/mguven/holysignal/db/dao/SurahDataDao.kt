package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.EditionData
import com.mguven.holysignal.db.entity.SurahData

@Dao
interface SurahDataDao {

  @Query("SELECT * from Surah")
  fun getAll(): LiveData<List<SurahData>>

  @Insert(onConflict = REPLACE)
  fun insert(surahData: SurahData)

  @Query("DELETE from Surah")
  fun deleteAll()
}