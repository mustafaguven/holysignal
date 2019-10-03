package com.mguven.holysignal.ui

import android.os.Bundle
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.rx.SchedulerProvider
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*
import javax.inject.Inject


class CardActivity : AbstractBaseActivity() {

  @Inject
  lateinit var schedulerProvider: SchedulerProvider

  private lateinit var holyBookViewModel: HolyBookViewModel
  private var isFavourite = false
  private var ayahNumber = 0

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(CardActivityModule(this))
        .inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_card)
    inject()
    holyBookViewModel = getViewModel(HolyBookViewModel::class.java)

    ayahNumber = (10..15).random()
    getAyahTopText()
    getAyahBottomText()
    getFavouriteStatus()

    ivPreferences.setOnClickListener {
      FlowController.launchMainActivity(this)
    }

    //holyBookViewModel.insertFavourite()
    //getFavourites()

    ivFavourite.setOnClickListener {
      isFavourite = !isFavourite
      upsertFavourite()
    }
  }

  private fun upsertFavourite() {
    holyBookViewModel.deleteFavourite(ayahNumber)
    if (isFavourite) {
      holyBookViewModel.insertFavourite(ayahNumber)
    }
  }

  private fun getFavouriteStatus() {
    holyBookViewModel.hasFavourite(ayahNumber).observe(this, Observer<List<FavouritesData>> { list ->
      isFavourite = list.isNotEmpty()
      ivFavourite.setImageResource(if (isFavourite) R.drawable.ic_star_fill_24px else R.drawable.ic_star_empty_24px)
    })
  }

  private fun getAyahTopText() {
    holyBookViewModel.getAyahTopText(ayahNumber).observe(this, Observer<List<SurahAyahSampleData>> { list ->
      list.forEach {
        cache.updateLastShownAyah(it)
        tvAyahNumber.text = "(${it.surahNumber}:${it.numberInSurah})"
        tvAyahTopText.text = "${it.language}: ${it.ayahText}"
      }
    })
  }

  private fun getAyahBottomText() {
    holyBookViewModel.getAyahBottomText(ayahNumber).observe(this, Observer<List<SurahAyahSampleData>> { list ->
      list.forEach {
        tvAyahBottomText.text = "${it.language}: ${it.ayahText}"
        tvSurah.text = "${it.surahEnglishName} (${it.surahEnglishNameTranslation})"
        tvRevelationType.text = it.surahRevelationType
      }
    })
  }

  /*private fun getAyahList(randomAyah: Int) {
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

  private fun getFavourites() {
    holyBookViewModel.getFavourites().observe(this, Observer<List<FavouritesData>> { list ->
      Log.e("AAA", "favourites ==============> list.size ${list.size}")
      list.forEach {
        Log.e("AAA", "favourites ==============> ${it.Id} == ${it.ayahNumber}")
      }
    })
  }
  */


}

