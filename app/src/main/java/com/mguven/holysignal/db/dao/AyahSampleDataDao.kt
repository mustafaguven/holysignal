package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.AyahSampleData

@Dao
interface AyahSampleDataDao {

  @Query("SELECT * from AyahSample WHERE editionId = :editionId")
  fun getAll(editionId: Int): LiveData<List<AyahSampleData>>

  @Insert(onConflict = REPLACE)
  fun insert(ayahData: AyahSampleData)

  @Query("DELETE from AyahSample")
  fun deleteAll()
}