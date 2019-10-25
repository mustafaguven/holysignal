package com.mguven.holysignal.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.Playmode
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.ui.adapter.AvailableSurahAdapter
import com.mguven.holysignal.ui.fragment.AddNoteFragment
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class CardActivity : AbstractBaseActivity(), AddNoteFragment.OnFragmentInteractionListener {


  private lateinit var holyBookViewModel: HolyBookViewModel

  private var playmode: Int = Int.MIN_VALUE
  private var availableSurahList: List<AvailableSurahItem>? = null

  private var spSurahOpeningClick = true

  private val playmodes by lazy {
    return@lazy resources.getStringArray(R.array.playmodes)
  }

  private lateinit var addNoteFragment: DialogFragment


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
    initPlaymode(playmode)
    initListeners()
  }

  private fun initData() {
    getAyahTopText()
    getAyahBottomText()
    showFavouriteStatus()
  }

  private fun getAyahNumberByPlaymode() = try {
    ivSelectSurah.setImageResource(R.drawable.ic_select_surah_disabled)
    when (playmode) {
      Playmode.RANDOM -> (1..cache.getMaxAyahCount()).random()
      Playmode.REPEAT_AYAH -> cache.getLastShownAyahNumber()
      Playmode.AYAH_BY_AYAH -> cache.getLastShownAyahNumber() + 1
      else -> onSelectSurah()
    }
  } catch (ex: Exception) {
    (1..cache.getMaxAyahCount()).random()
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
            " ${cache.getLastShownAyah()?.ayahText} *** ${tvAyahBottomText.text} ${getString(R.string.via_holy_signal)}"
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
      val newPlayMode = (playmode + 1) % playmodes.size
      initPlaymode(newPlayMode)
      ivSelectSurah.setImageResource(if (newPlayMode == Playmode.REPEAT_SURAH) R.drawable.ic_select_surah else R.drawable.ic_select_surah_disabled)
      clNextAyah.visibility = if (newPlayMode == Playmode.REPEAT_AYAH) View.GONE else View.VISIBLE
      cache.updatePlaymode(newPlayMode)
      playmode = newPlayMode
      showSnackbar(playmodes[newPlayMode])
    }

    ivAddNote.setOnClickListener {
      addNoteFragment = AddNoteFragment.newInstance(cache.getLastShownAyah()?.noteId)
      addNoteFragment.show(supportFragmentManager, addNoteFragment.javaClass.simpleName)
    }

    ivSelectSurah.setOnClickListener {
      spSurahOpeningClick = true
      if (playmode != Playmode.REPEAT_SURAH) return@setOnClickListener
      if (availableSurahList == null) {
        lifecycleScope.launch {
          holyBookViewModel.getAvailableSurahList().also { list ->
            if (availableSurahList != list) {
              availableSurahList = list
              updateAvailableSurahListAdapter(list)
            }
          }
        }
      } else {
        updateAvailableSurahListAdapter(availableSurahList!!)
      }
    }

    ivSearch.setOnClickListener {
      Toast.makeText(this, "Hazir degil", Toast.LENGTH_SHORT).show()
    }

    clNextAyah.setOnClickListener {
      ayahNumber = getAyahNumberByPlaymode()
      initData()
    }
  }

  private fun initPlaymode(mode: Int) {
    ivPlayMode.setImageResource(
        when (mode) {
          Playmode.RANDOM -> R.drawable.ic_random_24px
          Playmode.REPEAT_SURAH -> R.drawable.ic_repeat_surah24px
          Playmode.REPEAT_AYAH -> R.drawable.ic_repeat_ayah_24px
          else -> R.drawable.ic_loop_24px
        }
    )
  }

  private fun updateAvailableSurahListAdapter(list: List<AvailableSurahItem>) {
    var selectedItem = 0
    list.forEachIndexed { index, it ->
      if (it.value == cache.getLastShownAyah()?.surahNumber) {
        selectedItem = index
        return@forEachIndexed
      }
    }

    spSurahList.adapter = AvailableSurahAdapter(this, R.layout.status_item, list, selectedItem)
    spSurahList.setSelection(selectedItem)
    spSurahList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(adapterView: AdapterView<*>?) {
        //do nothing
      }

      override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!spSurahOpeningClick) {
          Log.e("AAA", "position ==> $position ---- min ==> ${availableSurahList!![position].min} ----- max ==> ${availableSurahList!![position].max}")
          val maxAyah = if (availableSurahList!![position].max >= cache.getMaxAyahCount()) cache.getMaxAyahCount() else availableSurahList!![position].max
          ayahNumber = (availableSurahList!![position].min..maxAyah).random()
          initData()
        }
        spSurahOpeningClick = false
      }
    }
    spSurahList.performClick()
  }

  private fun upsertFavourite() = runBlocking {
    holyBookViewModel.deleteFavourite(ayahNumber)
    if (isFavourite) {
      holyBookViewModel.insertFavourite(ayahNumber)
    }
    showFavouriteStatus()
  }

  private fun showFavouriteStatus() {
    lifecycleScope.launch {
      holyBookViewModel.hasFavourite(ayahNumber).also { list ->
        isFavourite = list.isNotEmpty()
        ivFavourite.setImageResource(if (isFavourite) R.drawable.ic_star_fill_24px else R.drawable.ic_star_empty_24px)
      }
    }
  }

  private fun getAyahTopText() {
    lifecycleScope.launch {
      val list = holyBookViewModel.getAyahTopText(ayahNumber)
      list.forEach {
        cache.updateLastShownAyah(it)
        tvAyahNumber.text = "(${it.surahNumber}:${it.numberInSurah})"
        tvAyahTopText.text = "${it.language}: ${it.ayahText}"
      }
    }
  }

  private fun getAyahBottomText() {
    lifecycleScope.launch {
      val list = holyBookViewModel.getAyahBottomText(ayahNumber)
      list.forEach {
        tvAyahBottomText.text = "${it.language}: ${it.ayahText}"
        tvSurah.text = "${it.surahEnglishName} (${it.surahEnglishNameTranslation})"
        tvRevelationType.text = it.surahRevelationType
      }
    }
  }

  override fun onNoteInserted(insertNo: Long) {
    lifecycleScope.launch {
      holyBookViewModel.updateNoteOfAyah(insertNo.toInt())
      addNoteFragment.dismiss()
      showSnackbar(getString(R.string.note_saved_successfully))
    }
  }
}

