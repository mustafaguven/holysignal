package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.mguven.holysignal.db.entity.FavouritesData

@Dao
interface FavouritesDataDao {

  @Query("SELECT * from Favourites")
  fun getAll(): LiveData<List<FavouritesData>>

  @Query("DELETE FROM Favourites")
  fun deleteAll()

  @Query("DELETE FROM Favourites WHERE ayahNumber = :ayahNumber")
  fun delete(ayahNumber: Int)

  @Insert(onConflict = REPLACE)
  fun insert(favouritesData: FavouritesData): Long

  @Query("SELECT * FROM Favourites WHERE ayahNumber = :ayahNumber")
  fun getByAyahNumber(ayahNumber: Int): LiveData<List<FavouritesData>>

}