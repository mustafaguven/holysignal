package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.EditionData
import com.mguven.holysignal.db.entity.SurahData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.rx.SchedulerProvider
import com.mguven.holysignal.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class MainActivity : AbstractBaseActivity() {

  @Inject
  lateinit var schedulerProvider: SchedulerProvider

  private lateinit var mainViewModel: MainViewModel


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    inject()
    mainViewModel = getViewModel(MainViewModel::class.java)

    getAyahList()

    //getEditionList()

    //getSurahList()

  }

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(MainActivityModule(this))
        .inject(this)
  }

  private fun getAyahList() {
    mainViewModel.getAyahList().observe(this, Observer<List<AyahSampleData>> { list ->
      Log.e("AAA", "==============> list.size ${list.size}")
      list.forEach {
        Log.e("AAA", "==============> ${it.Id} == ${it.text}")
      }
    })
  }

  private fun getSurahList() {
    mainViewModel.getSurahList().observe(this, Observer<List<SurahData>> { list ->
      Log.e("AAA", "==============> list.size ${list.size}")
      list.forEach {
        Log.e("AAA", "==============> ${it.Id} == ${it.englishName}")
      }
    })

  }

  private fun getEditionList() {
    ApplicationDatabase(this).editionDataDao().getAll().observe(this, Observer<List<EditionData>> { list ->
      Log.e("AAA", "==============> list.size ${list.size}")
      list.forEach {
        Log.e("AAA", "==============> ${it.Id} == ${it.identifier}")
      }
    })
  }

  private fun deleteAll() = runBlocking {
    launch(Dispatchers.Default) {
      Log.e("AAA", "==============> DELETE ${Thread.currentThread().name}")
      ApplicationDatabase(this@MainActivity).editionDataDao().deleteAll()
    }
  }

  private fun insert() = runBlocking {
    launch(Dispatchers.Default) {
      for (x in 200..1000) {
        Log.e("AAA", "==============> INSERT ${Thread.currentThread().name}")
        ApplicationDatabase(this@MainActivity).editionDataDao().insert(EditionData(x, "d", "tr", "name", "english name here", "text", "translation"))
      }
    }
  }



}

