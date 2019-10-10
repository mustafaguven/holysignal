package com.mguven.holysignal.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.Playmode
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.db.entity.FavouritesData
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.ui.adapter.AvailableSurahAdapter
import com.mguven.holysignal.ui.fragment.AddNoteFragment
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*


class CardActivity : AbstractBaseActivity(), AddNoteFragment.OnFragmentInteractionListener {


  private lateinit var holyBookViewModel: HolyBookViewModel

  private var playmode: Int = Int.MIN_VALUE
  private var availableSurahList: List<AvailableSurahItem>? = null

  private val playmodes by lazy {
    return@lazy resources.getStringArray(R.array.playmodes)
  }

  private val addNoteFragment by lazy {
    return@lazy AddNoteFragment.newInstance(cache.getLastShownAyah()?.noteId)
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
    playmode = cache.getPlaymode()
    ayahNumber = getAyahNumberByPlaymode()
    initData()
    initPlaymode()
    initListeners()
  }

  private fun initData() {
    getAyahTopText()
    getAyahBottomText()
    getFavouriteStatus()
  }

  private fun getAyahNumberByPlaymode(): Int {
    return try {
      ivSelectSurah.setImageResource(R.drawable.ic_select_surah_disabled)
      return when (playmode) {
        Playmode.RANDOM -> (1..cache.getMaxAyahCount()).random()
        Playmode.REPEAT_AYAH -> cache.getLastShownAyahNumber()
        else -> return onSelectSurah()
      }
    } catch (ex: Exception) {
      (1..cache.getMaxAyahCount()).random()
    }
  }

  private fun onSelectSurah(): Int {
    ivSelectSurah.setImageResource(R.drawable.ic_select_surah)
    return if (cache.getLastShownAyah()!!.endingAyahNumber <= cache.getMaxAyahCount()) {
      (cache.getLastShownAyah()!!.startingAyahNumber..cache.getLastShownAyah()!!.endingAyahNumber).random()
    } else {
      (cache.getLastShownAyah()!!.startingAyahNumber..cache.getMaxAyahCount()).random()
    }
  }

  private fun initListeners() {
    ivPreferences.setOnClickListener {
      FlowController.launchMainActivity(this)
    }

    ivFavourite.setOnClickListener {
      isFavourite = !isFavourite
      upsertFavourite()
      showSnackbar(if (isFavourite) getString(R.string.added_to_favourites) else getString(R.string.removed_from_favourites))
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
      val newPlayMode = (playmode + 1) % 3
      ivPlayMode.setImageResource(
          if (newPlayMode == Playmode.RANDOM) R.drawable.ic_random_24px else {
            if (newPlayMode == Playmode.REPEAT_SURAH) R.drawable.ic_repeat_surah24px else R.drawable.ic_repeat_ayah_24px
          })

      ivSelectSurah.setImageResource(if (newPlayMode == Playmode.REPEAT_SURAH) R.drawable.ic_select_surah else R.drawable.ic_select_surah_disabled)
      cache.updatePlaymode(newPlayMode)
      playmode = newPlayMode
      showSnackbar(playmodes[newPlayMode])
    }

    ivAddNote.setOnClickListener {
      addNoteFragment.show(supportFragmentManager, addNoteFragment.javaClass.simpleName)
    }

    ivSelectSurah.setOnClickListener {
      if (playmode != Playmode.REPEAT_SURAH) return@setOnClickListener
      if (availableSurahList == null) {
        holyBookViewModel.getAvailableSurahList().observe(this, Observer<List<AvailableSurahItem>> { list ->
          availableSurahList = list
          updateAvailableSurahListAdapter(list)
        })
      } else {
        updateAvailableSurahListAdapter(availableSurahList!!)
      }
    }
  }

  private fun updateAvailableSurahListAdapter(list: List<AvailableSurahItem>) {
    val adapter = AvailableSurahAdapter(this, R.layout.status_item, list)
    spSurahList.adapter = adapter
/*    list.forEachIndexed { index, it ->
      if (it.value == cache.getLastShownAyah()?.surahNumber) {
        spSurahList.setSelection(index)
        return@forEachIndexed
      }
    }*/
    spSurahList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(adapterView: AdapterView<*>?) {
        val a = 0
      }

      override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        ayahNumber = (availableSurahList!![position].min..availableSurahList!![position].max).random()
        initData()
      }
    }
    spSurahList.performClick()
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

  override fun onNoteInserted(insertNo: Long) {
    holyBookViewModel.updateNoteOfAyah(insertNo.toInt()).observe(this, Observer {
      addNoteFragment.dismiss()
      showSnackbar(getString(R.string.note_saved_successfully))
    })
  }
}

