package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.ViewingCountsData

@Dao
interface ViewingCountsDataDao {

  @Query("SELECT * from ViewingCounts")
  suspend fun getAll(): List<ViewingCountsData>

  @Query("SELECT ayahNumber from ViewingCounts")
  suspend fun getAllAyahNumbersAsList(): List<Long>

  @Query("DELETE FROM ViewingCounts")
  suspend fun deleteAll()

  @Query("DELETE FROM ViewingCounts WHERE ayahNumber = :ayahNumber")
  suspend fun delete(ayahNumber: Int)

  @Insert(onConflict = REPLACE)
  suspend fun insert(viewingCountsDataDao: ViewingCountsData): Long

  @Query("SELECT * FROM ViewingCounts WHERE ayahNumber = :ayahNumber")
  suspend fun getByAyahNumber(ayahNumber: Int): List<ViewingCountsData>

  @Query("UPDATE ViewingCounts SET count = :count WHERE ayahNumber = :ayahNumber")
  suspend fun update(count: Int, ayahNumber: Int)

}