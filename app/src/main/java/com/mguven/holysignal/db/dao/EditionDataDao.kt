package com.mguven.holysignal.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.mguven.holysignal.db.entity.BooksBySelectedLanguageData
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.EditionData

@Dao
interface EditionDataDao {

  @Query("SELECT * from Edition")
  fun getAll(): LiveData<List<EditionData>>

  @Query("SELECT A.max as max, L.englishVersion || ' > ' || name as 'key', E.Id as value " +
      "      from Edition E left join (SELECT distinct editionId, max(number) as max From AyahSample group by editionId) A " +
      "      on E.Id = A.editionId " +
      "   INNER JOIN Language L on L.abbreviation = E.language " +
      "       WHERE type in ('translation', 'quran', 'transliteration') AND max >= :maxCount order by L.abbreviation, max desc, type")
  suspend fun getNameIdList(maxCount: Int): List<EditionAdapterData>

  @Query("SELECT A.max as max, E.Id || ' ' || name || ' ' || '(' || language || ')' as 'key', E.Id as value " +
      "from Edition E left join (SELECT distinct editionId, max(number) as max From AyahSample " +
      "group by editionId) A " +
      "on E.Id = A.editionId " +
      " WHERE type in ('translation', 'quran', 'transliteration') order by max desc, type  ")
  suspend fun getDownloadableEditions(): List<EditionAdapterData>

  @Query("SELECT E.Id, E.name  FROM Language L INNER JOIN Edition E on L.abbreviation = E.language " +
      "WHERE E.type in ('translation', 'transliteration', 'quran') " +
      "AND L.Id = :languageId ORDER BY name")
  suspend fun getEditionsBySelectedLanguage(languageId: Int): List<BooksBySelectedLanguageData>

  @Insert(onConflict = REPLACE)
  fun insert(editionData: EditionData)

  @Query("DELETE from Edition")
  fun deleteAll()
}