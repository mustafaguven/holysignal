package com.mguven.holysignal.viewmodel

import androidx.sqlite.db.SimpleSQLiteQuery
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.NotesData
import javax.inject.Inject


class HolyBookViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  suspend fun getAyahTopText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getTopTextEditionId(), randomAyahNumber)

  suspend fun getAyahBottomText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getBottomTextEditionId(), randomAyahNumber)

  suspend fun deleteFavourite(ayahNumber: Int) = database.favouritesDataDao().delete(ayahNumber)

  suspend fun insertFavourite(ayahNumber: Int) =
      database.favouritesDataDao().insert(FavouritesData(0, ayahNumber))

  suspend fun getFavourites() = database.favouritesDataDao().getAll()

  suspend fun hasFavourite(ayahNumber: Int) = database.favouritesDataDao().getByAyahNumber(ayahNumber)

  suspend fun getNoteById(noteId: Int) = database.notesDataDao().getNoteById(noteId)

  private suspend fun deleteNote(noteId: Int) = database.notesDataDao().delete(noteId)

  private suspend fun insertNote(content: String) =
      database.notesDataDao().insert(NotesData(0, content))

  suspend fun upsertNote(noteId: Int, content: String): Long {
    deleteNote(noteId)
    return insertNote(content)
  }

  suspend fun updateNoteOfAyah(noteId: Int) {
    cache.getLastShownAyah().run {
      database.ayahSampleDataDao().updateNoteId(this!!.ayahId, noteId)
      this.noteId = noteId
      cache.updateLastShownAyah(this)
    }
  }

  suspend fun getAvailableSurahList() =
      database.surahDataDao().getAvailableSurahListByEditionId(cache.getTopTextEditionId())

  suspend fun getAyahsByKeywords(editionId: Int, words: List<String>): List<Int> {
    val query = SimpleSQLiteQuery("""
      SELECT number FROM AyahSample WHERE editionId = $editionId 
       AND (${getLikeCriteria(words)})
    """.trimMargin())
    return database.ayahSampleDataDao().getAyahsByKeyword(query)
  }

  private fun getLikeCriteria(words: List<String>): String {
    val criteria = StringBuilder()
    words.forEach { criteria.append(" text like '%$it%' OR ") }
    return criteria.toString().dropLast(3)
  }


}
