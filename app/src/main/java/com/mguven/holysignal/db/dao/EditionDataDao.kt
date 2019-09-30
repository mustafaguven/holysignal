package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.EditionData

@Dao
interface EditionDataDao {

  @Query("SELECT * from Edition")
  fun getAll(): LiveData<List<EditionData>>

  @Insert(onConflict = REPLACE)
  fun insert(editionData: EditionData)

  @Query("DELETE from Edition")
  fun deleteAll()
}