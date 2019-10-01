package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.db.ApplicationDatabase
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.EditionData
import com.mguven.holysignal.db.entity.SurahData
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.job.LockScreenJob
import com.mguven.holysignal.rx.SchedulerProvider
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class CardActivity : AbstractBaseActivity() {

  @Inject
  lateinit var schedulerProvider: SchedulerProvider

  private lateinit var holyBookViewModel: HolyBookViewModel


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.e("ScreenActionReceiver", "screen is card activity")
    setContentView(R.layout.activity_card)
    inject()
    holyBookViewModel = getViewModel(HolyBookViewModel::class.java)



    getRandomAyah()

    //getEditionList()

    //getSurahList()

  }



  private fun getRandomAyah() {
    val randomAyahNumber = (0..50).random()
    val editionId = 53
    holyBookViewModel.getRandomAyah(editionId, randomAyahNumber).observe(this, Observer<List<AyahSampleData>> { list ->
      list.forEach {
        tvAyah.text = it.text
        Log.e("AAA", "==============> ${it.Id} == ${it.text}")
      }
    })
  }

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(CardActivityModule(this))
        .inject(this)
  }

  private fun getAyahList(editionId: Int, randomAyah: Int) {
    holyBookViewModel.getAyahList().observe(this, Observer<List<AyahSampleData>> { list ->
      Log.e("AAA", "==============> list.size ${list.size}")
      list.forEach {
        Log.e("AAA", "==============> ${it.Id} == ${it.text}")
      }
    })
  }

  private fun getSurahList() {
    holyBookViewModel.getSurahList().observe(this, Observer<List<SurahData>> { list ->
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
      ApplicationDatabase(this@CardActivity).editionDataDao().deleteAll()
    }
  }

  private fun insert() = runBlocking {
    launch(Dispatchers.Default) {
      for (x in 200..1000) {
        Log.e("AAA", "==============> INSERT ${Thread.currentThread().name}")
        ApplicationDatabase(this@CardActivity).editionDataDao().insert(EditionData(x, "d", "tr", "name", "english name here", "text", "translation"))
      }
    }
  }


}

