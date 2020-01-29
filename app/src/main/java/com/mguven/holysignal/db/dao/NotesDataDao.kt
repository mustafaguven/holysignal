package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.NotesData

@Dao
interface NotesDataDao {

  @Query("SELECT * from Notes")
  fun getAll(): LiveData<List<NotesData>>

  @Query("DELETE FROM Notes")
  fun deleteAll()

  @Query("DELETE FROM Notes WHERE Id = :id")
  suspend fun delete(id: Int)

  @Insert(onConflict = REPLACE)
  suspend fun insert(notesData: NotesData): Long

  @Query("SELECT * FROM Notes WHERE Id = :id")
  suspend fun getNoteById(id: Int): List<NotesData>

}