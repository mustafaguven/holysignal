package com.mguven.holysignal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.NotesData
import com.mguven.holysignal.db.entity.ViewingCountsData
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.model.request.RequestAddFavourites
import com.mguven.holysignal.model.request.RequestAddNote
import com.mguven.holysignal.model.request.RequestInsertVoter
import com.mguven.holysignal.model.request.RequestRemoveNote
import com.mguven.holysignal.model.response.AddNoteEntity
import com.mguven.holysignal.model.response.InsertVoterEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
import com.mguven.holysignal.model.response.RemoveNoteEntity
import com.mguven.holysignal.network.FavouritesApi
import com.mguven.holysignal.network.NotesApi
import com.mguven.holysignal.util.DeviceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class HolyBookViewModel @Inject
constructor(private val database: ApplicationDatabase,
            val cache: ApplicationCache,
            private val favouritesApi: FavouritesApi,
            private val notesApi: NotesApi,
            private val deviceUtil: DeviceUtil) : BaseViewModel() {

  val totalFavouriteCount = MutableLiveData<Int>()
  val allNotesFromCloud = MutableLiveData<GetNotesByAyahNumberEntity>()
  val changeAyahNoteVoteCountObserver = MutableLiveData<InsertVoterEntity>()
  val addNoteObserver = MutableLiveData<AddNoteEntity>()
  val removeNoteObserver = MutableLiveData<RemoveNoteEntity>()

  private var favouriteIdList: List<Long>? = null
  fun getFavouriteIdList(): List<Long>? {
    if (!favouriteIdList.isNotNullAndNotEmpty()) {
      favouriteIdList = cache.getFavouriteAyahs()?.ayahNumbers
      if (!favouriteIdList.isNotNullAndNotEmpty()) {
        runBlocking {
          favouriteIdList = getFavouritesOnlyAyahNumbers()
        }
      }
    }
    return favouriteIdList
  }

  suspend fun getAyahTopText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getTopTextEditionId(), randomAyahNumber)

  suspend fun getAyahBottomText(randomAyahNumber: Int) =
      database.ayahSampleDataDao().getRandomAyah(cache.getBottomTextEditionId(), randomAyahNumber)

  suspend fun deleteFavourite(ayahNumber: Int) = database.favouritesDataDao().delete(ayahNumber)

  suspend fun insertFavourite(ayahNumber: Int) =
      database.favouritesDataDao().insert(FavouritesData(0, ayahNumber))

  suspend fun getFavourites() = database.favouritesDataDao().getAll()

  private suspend fun getFavouritesOnlyAyahNumbers() = database.favouritesDataDao().getAllAyahNumbersAsList()

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

  fun clearFavouriteCache() {
    favouriteIdList = null
    cache.updateFavouriteAyahs(null)
  }

  suspend fun getViewingCount(ayahNumber: Int): Int {
    val list = database.viewingCountsDataDao().getByAyahNumber(ayahNumber)
    var count = 1
    if (list.isNotNullAndNotEmpty()) {
      count = list[0].count + 1
      database.viewingCountsDataDao().update(count, ayahNumber)
    } else {
      database.viewingCountsDataDao().insert(ViewingCountsData(0, ayahNumber, count))
    }
    return count
  }

  suspend fun getTotalViewingCount() = database.viewingCountsDataDao().getTotalCount()

  fun addFavouriteToCloud(ayahNumber: Int, isAdd: Int = 1) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        favouritesApi.addFavourite(RequestAddFavourites(cache.getToken(), ayahNumber, isAdd))
      }
    }
  }

  fun getFavouriteCountByAyahNumber(ayahNumber: Int) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = favouritesApi.getFavouriteCountByAyahNumber(ayahNumber)
        totalFavouriteCount.postValue(response.data)
      }
    }
  }

  fun getNotesByAyahNumber(ayahNumber: Int) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val response = notesApi.getNotesByAyahNumber(cache.getMemberId(), ayahNumber)
        allNotesFromCloud.postValue(response)
      }
    }
  }

  fun insertVoter(ayahNoteId: Int, vote: Int) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val result = notesApi.insertVoter(RequestInsertVoter(cache.getMemberId(), ayahNoteId, vote))
        changeAyahNoteVoteCountObserver.postValue(result)
      }
    }
  }

  fun addNote(ayahNumber: Int, note: String) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val result = notesApi.addNote(RequestAddNote(cache.getMemberId(), ayahNumber, note))
        addNoteObserver.postValue(result)
      }
    }
  }

  fun removeAyah(ayahNoteId: Int) {
    if (deviceUtil.isConnected()) {
      CoroutineScope(Dispatchers.IO).launch {
        val result = notesApi.removeNote(RequestRemoveNote(ayahNoteId))
        removeNoteObserver.postValue(result)
      }
    }
  }
}
