package com.mguven.holysignal.viewmodel

import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.FavouritesData
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

}
