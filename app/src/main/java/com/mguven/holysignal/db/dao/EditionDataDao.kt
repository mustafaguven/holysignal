package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.EditionData

@Dao
interface EditionDataDao {

  @Query("SELECT * from Edition")
  fun getAll(): LiveData<List<EditionData>>

  @Query("SELECT name || ' ' || '(' || language || ')' as 'key', Id as value from Edition WHERE type = 'translation'")
  fun getNameIdList(): LiveData<List<EditionAdapterData>>

  @Insert(onConflict = REPLACE)
  fun insert(editionData: EditionData)

  @Query("DELETE from Edition")
  fun deleteAll()
}