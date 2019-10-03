package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.DisplayModeData

@Dao
interface DisplayModeDataDao {

  @Query("SELECT * from DisplayMode")
  fun getAll(): LiveData<List<DisplayModeData>>

  @Insert(onConflict = REPLACE)
  fun upsert(displayMode: DisplayModeData)

}