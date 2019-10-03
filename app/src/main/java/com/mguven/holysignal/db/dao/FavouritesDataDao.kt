package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.FavouritesData

@Dao
interface FavouritesDataDao {

  @Query("SELECT * from Favourites")
  fun getAll(): LiveData<List<FavouritesData>>

  @Insert(onConflict = REPLACE)
  fun upsert(favouritesData: FavouritesData)

}