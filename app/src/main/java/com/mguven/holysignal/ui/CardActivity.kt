package com.mguven.holysignal.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.Playmode
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*


class CardActivity : AbstractBaseActivity() {

  private lateinit var holyBookViewModel: HolyBookViewModel

  private val playmode by lazy {
    return@lazy cache.getPlaymode()
  }

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
    Log.e("AAA", "card activity is on")
    holyBookViewModel = getViewModel(HolyBookViewModel::class.java)

    ayahNumber = getAyahNumberByPlaymode()

    getAyahTopText()
    getAyahBottomText()
    getFavouriteStatus()
    initPlaymode()
    initListeners()
  }

  private fun getAyahNumberByPlaymode(): Int {
    return try {
      when (playmode) {
        Playmode.RANDOM -> (1..cache.getMaxAyahCount()).random()
        Playmode.REPEAT_AYAH -> cache.getLastShownAyahNumber()
        else ->
          (cache.getLastShownAyah()!!.startingAyahNumber..cache.getLastShownAyah()!!.endingAyahNumber).random()
      }
    } catch (ex: Exception) {
      (1..cache.getMaxAyahCount()).random()
    }
  }

  private fun initListeners() {
    ivPreferences.setOnClickListener {
      FlowController.launchMainActivity(this)
    }

    ivFavourite.setOnClickListener {
      isFavourite = !isFavourite
      upsertFavourite()
    }

    ivShare.setOnClickListener {
      whenNotNull(cache.getLastShownAyah()) {
        val shareText = "(${cache.getLastShownAyah()?.surahNumber}:${cache.getLastShownAyah()?.numberInSurah})" +
            " ${cache.getLastShownAyah()?.ayahText} (via www.holysignal.com)"
        val sendIntent: Intent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, shareText)
          type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
      }
    }

    ivPlayMode.setOnClickListener {
      val newPlayMode = (cache.getPlaymode() + 1) % 3
      ivPlayMode.setImageResource(
          if (newPlayMode == 0) R.drawable.ic_random_24px else {
            if (newPlayMode == 1) R.drawable.ic_repeat_surah24px else R.drawable.ic_repeat_ayah_24px
          })
      cache.updatePlaymode(newPlayMode)
    }
  }

  private fun initPlaymode() {
    ivPlayMode.setImageResource(
        if (playmode == Playmode.RANDOM) R.drawable.ic_random_24px else {
          if (playmode == Playmode.REPEAT_SURAH) R.drawable.ic_repeat_surah24px else R.drawable.ic_repeat_ayah_24px
        })
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

