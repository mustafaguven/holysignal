package com.mguven.holysignal.viewmodel

import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.ApplicationDatabase
import javax.inject.Inject


class DownloadViewModel @Inject
constructor(private val database: ApplicationDatabase,
            private val cache: ApplicationCache) : BaseViewModel() {

  suspend fun getDownloadableEditions() =
    database.editionDataDao().getDownloadableEditions()



}
