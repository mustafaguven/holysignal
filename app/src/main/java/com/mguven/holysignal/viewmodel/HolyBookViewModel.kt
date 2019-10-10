package com.mguven.holysignal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.NotesData
import com.mguven.holysignal.inline.whenNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class HolyBookViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  fun getAyahTopText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getTopTextEditionId(), randomAyahNumber)

  fun getAyahBottomText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getBottomTextEditionId(), randomAyahNumber)

  fun deleteFavourite(ayahNumber: Int) = runBlocking {
    launch(Dispatchers.Default) {
      database.favouritesDataDao().delete(ayahNumber)
    }
  }

  fun insertFavourite(ayahNumber: Int) = runBlocking {
    launch(Dispatchers.Default) {
      database.favouritesDataDao().insert(FavouritesData(0, ayahNumber))
    }
  }

  fun getFavourites() =
      database.favouritesDataDao().getAll()

  fun hasFavourite(ayahNumber: Int) = database.favouritesDataDao().getByAyahNumber(ayahNumber)

  fun getNoteById(noteId: Int) = database.notesDataDao().getNoteById(noteId)

  fun deleteNote(noteId: Int) = runBlocking {
    launch(Dispatchers.Default) {
      database.notesDataDao().delete(noteId)
    }
  }

  fun insertNote(content: String) = liveData {
    emit(database.notesDataDao().insert(NotesData(0, content)))
  }

  fun upsertNote(noteId: Int, content: String): LiveData<Long> {
    deleteNote(noteId)
    return insertNote(content)
  }

  fun updateNoteOfAyah(noteId: Int) = liveData {
    whenNotNull(cache.getLastShownAyah()) {
      emit(database.ayahSampleDataDao().updateNoteId(it.ayahId, noteId))
    }
  }

  fun getAvailableSurahList() =
      database.surahDataDao().getAvailableSurahListByEditionId(cache.getTopTextEditionId())

}
