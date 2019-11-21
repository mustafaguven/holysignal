package com.mguven.holysignal.viewmodel

import androidx.lifecycle.LiveData
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.MaxAyahCountData
import com.mguven.holysignal.network.SurahApi
import com.mguven.holysignal.paging.NetworkState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class PreferencesViewModel @Inject
constructor(private val surahApi: SurahApi,
            private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  fun getMaxAyahCount(): LiveData<MaxAyahCountData> {
    return database.ayahSampleDataDao().getMaxAyahCountByEditionId(cache.getTopTextEditionId())
  }

  fun getEditionNameIdList(): LiveData<List<EditionAdapterData>> {
    return database.editionDataDao().getNameIdList()
  }

  suspend fun getDownloadableEditions() =
    database.editionDataDao().getDownloadableEditions()

  fun startDownload(editionId: Int) {
    compositeDisposable.add(
        surahApi.getSurahByEditionId(1, editionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                  val a = 0
                },
                {
                  Timber.e(it)
                  val b = 0
                }
            ))
  }


}
