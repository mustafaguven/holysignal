package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.mguven.holysignal.db.entity.LanguageData

@Dao
interface LanguageDataDao {

  @Query("SELECT * from Language")
  suspend fun getAll(): List<LanguageData>

}