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

  @Query("SELECT A.max as max, E.Id || ' ' || name || ' ' || '(' || language || ')' as 'key', E.Id as value \n" +
      "from Edition E left join (SELECT distinct editionId, max(number) as max From AyahSample\n" +
      "group by editionId) A\n" +
      "on E.Id = A.editionId\n" +
      " WHERE type in ('translation', 'quran', 'transliteration') order by max desc, type ")
  fun getNameIdList(): LiveData<List<EditionAdapterData>>

  @Query("SELECT A.max as max, E.Id || ' ' || name || ' ' || '(' || language || ')' as 'key', E.Id as value \n" +
      "from Edition E left join (SELECT distinct editionId, max(number) as max From AyahSample\n" +
      "group by editionId) A\n" +
      "on E.Id = A.editionId\n" +
      " WHERE type in ('translation', 'quran', 'transliteration') order by max desc, type  ")
  suspend fun getDownloadableEditions(): List<EditionAdapterData>

  @Insert(onConflict = REPLACE)
  fun insert(editionData: EditionData)

  @Query("DELETE from Edition")
  fun deleteAll()
}