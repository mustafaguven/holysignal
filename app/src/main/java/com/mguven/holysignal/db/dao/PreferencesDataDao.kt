package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.PreferencesData

@Dao
interface PreferencesDataDao {

  @Query("SELECT * from Preferences")
  fun getAll(): LiveData<List<PreferencesData>>

  @Insert(onConflict = REPLACE)
  fun upsert(preferencesData: PreferencesData)

  @Query("UPDATE Preferences SET topTextEditionId = :topEditionId, bottomTextEditionId = :bottomEditionId")
  fun upsertEditionId(topEditionId: Int, bottomEditionId: Int)

}