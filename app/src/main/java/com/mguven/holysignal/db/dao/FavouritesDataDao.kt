package com.mguven.holysignal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.FavouritesData

@Dao
interface FavouritesDataDao {

  @Query("SELECT * from Favourites")
  suspend fun getAll(): List<FavouritesData>

  @Query("SELECT ayahNumber from Favourites")
  suspend fun getAllAyahNumbersAsList(): List<Long>

  @Query("DELETE FROM Favourites")
  suspend fun deleteAll()

  @Query("DELETE FROM Favourites WHERE ayahNumber = :ayahNumber")
  suspend fun delete(ayahNumber: Int)

  @Insert(onConflict = REPLACE)
  suspend fun insert(favouritesData: FavouritesData): Long

  @Query("SELECT * FROM Favourites WHERE ayahNumber = :ayahNumber")
  suspend fun getByAyahNumber(ayahNumber: Int): List<FavouritesData>

}