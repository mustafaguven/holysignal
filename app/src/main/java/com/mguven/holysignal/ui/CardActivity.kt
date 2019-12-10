package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.constant.Playmode
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.model.AyahSearchResult
import com.mguven.holysignal.ui.adapter.AyahViewPagerAdapter
import com.mguven.holysignal.ui.fragment.BaseDialogFragment
import com.mguven.holysignal.ui.fragment.NotesFragment
import com.mguven.holysignal.ui.fragment.SearchWordInAyahsFragment
import com.mguven.holysignal.ui.fragment.SelectSurahFragment
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random


class CardActivity : AbstractBaseActivity(),
    SearchWordInAyahsFragment.OnFragmentInteractionListener,
    SelectSurahFragment.OnFragmentInteractionListener {

  companion object {
    private const val MAX_SEARCH_KEYWORD_THRESHOLD = 3
    private const val FAVOURITE_STARTER_AYAH_ID = -100
    private const val AYAH_SET_MAX_SIZE = 5
    private const val SAVED_AYAH_NUMBER = "SAVED_AYAH_NUMBER"
  }

  //injections
  @Inject
  lateinit var deviceUtil: DeviceUtil

  @Inject
  lateinit var ayahViewPagerAdapter: AyahViewPagerAdapter
  private lateinit var holyBookViewModel: HolyBookViewModel

  //fragments
  private lateinit var notesFragment: BaseDialogFragment
  private lateinit var searchWordInAyahsFragment: BaseDialogFragment
  private lateinit var selectSurahFragment: BaseDialogFragment

  //other
  private var ayahSet = mutableSetOf<Int>()
  private var ayahNumber = 0
  private var isFavourite = false
  private var playmode: Int = Int.MIN_VALUE
  private val playmodes by lazy {
    return@lazy resources.getStringArray(R.array.playmodes)
  }

  private fun isFavourite() = ayahNumber == FAVOURITE_STARTER_AYAH_ID

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
    playmode = cache.getPlaymode()
    initPlaymode(playmode)
    populateAyahSet(playmode)
    ayahNumber = savedInstanceState?.getInt(SAVED_AYAH_NUMBER) ?: getAyahNumberByPlaymode()
    //initData()

    viewpager.adapter = ayahViewPagerAdapter
    initListeners()
  }

  private fun pageChanged() {
    if (!isFavourite()) {
      //holyBookViewModel.getFavouriteCountByAyahNumber(ayahNumber)
      ivFavourite.visibility = View.VISIBLE
      ivBookMarkAyah.setImageResource(if (cache.getBookmark() == ayahNumber) R.drawable.ic_bookmark_filled_24px else R.drawable.ic_bookmark_empty_24px)
      ivAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
      getViewingPercentage()
      showFavouriteStatus()
    }
  }

  private fun getAyahNumberByPlaymode(): Int {
    try {
      val searchCriteriaResult = cache.getAyahSearchResult()
      if (searchCriteriaResult != null && searchCriteriaResult.list.isNotNullAndNotEmpty()) {
        ++searchCriteriaResult.lastIndex
        cache.updateAyahSearchResult(searchCriteriaResult)
        arrangeViewsBySearch(searchCriteriaResult)
        return searchCriteriaResult.list!![searchCriteriaResult.lastIndex % searchCriteriaResult.list.size]
      }

      tvKeywords.visibility = View.GONE

      populateAyahSet(playmode)
      return ayahSet.elementAt(0)

    } catch (ex: Exception) {
      return (1..cache.getMaxAyahCount()).random()
    }
  }

  private fun populateAyahSet(playmode: Int, pagerPosition: Int = 0) {
    if (ayahSet.isEmpty() || (ayahSet.size - 1 == pagerPosition)) {
      val newAyahSet = when (playmode) {
        Playmode.RANDOM -> List(AYAH_SET_MAX_SIZE) { Random.nextInt(0, cache.getMaxAyahCount()) }.toMutableSet()
        Playmode.REPEAT_AYAH -> (1..AYAH_SET_MAX_SIZE).map { cache.getLastShownAyahNumber() }.toMutableSet()
        Playmode.AYAH_BY_AYAH -> {
          (cache.getLastShownAyahNumber()..cache.getLastShownAyahNumber() + AYAH_SET_MAX_SIZE).map { it }.toMutableSet()
        }
        Playmode.FAVOURITES ->
          List(AYAH_SET_MAX_SIZE) { Random.nextInt(0, cache.getMaxAyahCount()) }.toMutableSet()
        Playmode.REPEAT_SURAH -> {
          populateAyahSetByRepeatSurah()
        }
        else -> {
          List(AYAH_SET_MAX_SIZE) { Random.nextInt(0, cache.getMaxAyahCount()) }.toMutableSet()
        }
      }

      ayahSet.addAll(newAyahSet)
      ayahViewPagerAdapter.updateAyahSet(ayahSet)
      ayahViewPagerAdapter.notifyDataSetChanged()

      Log.e("AYAH_SET", "Playmode: $playmode set: $ayahSet")
    }
  }

  private fun populateAyahSetByRepeatSurah(ayahNumber: Int, startingNumber: Int, endingNumber: Int): MutableSet<Int> {
    var topLimit = cache.getLastShownAyahNumber() + AYAH_SET_MAX_SIZE
    val max = cache.getLastShownAyah()!!.endingAyahNumber
    if (topLimit > max) {
      topLimit = max
    }
    val returnSet = (cache.getLastShownAyahNumber()..topLimit)
        .toMutableSet()
    if (returnSet.size < AYAH_SET_MAX_SIZE) {
      val diff = AYAH_SET_MAX_SIZE - returnSet.size
      val additionalSet = (cache.getLastShownAyah()!!.startingAyahNumber..cache.getLastShownAyah()!!.startingAyahNumber + diff)
          .map { it }.toMutableSet()
      returnSet.addAll(additionalSet)
    }
    return returnSet
  }

//  private fun byFavourites(): Int {
//    val favouriteIdList = holyBookViewModel.getFavouriteIdList()
//    if (favouriteIdList.isNotNullAndNotEmpty()) {
//      tvKeywords.visibility = View.VISIBLE
//      tvKeywords.text = getString(R.string.favourites_are_getting_randomly)
//      val randomIndex = getRandomFavouriteIndex(favouriteIdList)
//      cache.updateLastShownFavouriteIndex(randomIndex)
//      return favouriteIdList!![randomIndex].toInt()
//    } else {
//      tvAyahTopText.text = getString(R.string.none_favourite_found)
//      ivFavourite.visibility = View.INVISIBLE
//      tvNext.isEnabled = false
//      tvPrevious.isEnabled = false
//      tvAyahBottomText.setEmpty()
//      tvAyahNumber.setEmpty()
//    }
//    return FAVOURITE_STARTER_AYAH_ID
//  }

  private fun getRandomFavouriteIndex(favouriteIdList: List<Long>?): Int {
    var index = (favouriteIdList!!.indices).random()
    if (cache.getLatestShownFavouriteIndex() == index && favouriteIdList.size > 1) {
      index = getRandomFavouriteIndex(favouriteIdList)
    }
    return index
  }

  private fun onSelectSurah(increment: Int): Int {
    var ayahNumber = cache.getLastShownAyahNumber() + increment
    if (ayahNumber > cache.getLastShownAyah()!!.endingAyahNumber) {
      ayahNumber = cache.getLastShownAyah()!!.startingAyahNumber
    } else if (ayahNumber < cache.getLastShownAyah()!!.startingAyahNumber) {
      ayahNumber = cache.getLastShownAyah()!!.endingAyahNumber
    }
    return ayahNumber
  }

  private fun initListeners() {
    ivPreferences.setOnClickListener {
      FlowController.launchMainActivity(this)
    }

    ivAllBookmarks.setOnClickListener {
      lifecycleScope.launch {
        val list = holyBookViewModel.getAyahTopText(cache.getBookmark())
        if (list.isNotNullAndNotEmpty()) {
          showYesNoDialog(getString(R.string.go_to_bookmark_warning_message, "${list[0].surahNumber}:${list[0].numberInSurah}"), DialogInterface.OnClickListener { dialog, yes ->
            ayahNumber = cache.getBookmark()
            pageChanged()
            dialog.dismiss()
          }, DialogInterface.OnClickListener { dialog, no ->
            dialog.dismiss()
          })
        }
      }
    }

    ivBookMarkAyah.setOnClickListener {
      val bookmarkedAyah = cache.getBookmark()
      if (ayahNumber != bookmarkedAyah) {
        showYesNoDialog(getString(if (bookmarkedAyah == ConstantVariables.EMPTY_BOOKMARK) R.string.new_bookmark_set_warning_message
        else R.string.bookmark_change_warning_message), DialogInterface.OnClickListener { dialog, yes ->
          cache.updateBookmark(ayahNumber)
          ivBookMarkAyah.setImageResource(R.drawable.ic_bookmark_filled_24px)
          ivAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
          dialog.dismiss()
        }, DialogInterface.OnClickListener { dialog, no ->
          dialog.dismiss()
        })
      } else {
        showYesNoDialog(getString(R.string.remove_bookmark_warning_message), DialogInterface.OnClickListener { dialog, yes ->
          cache.updateBookmark(ConstantVariables.EMPTY_BOOKMARK)
          ivBookMarkAyah.setImageResource(R.drawable.ic_bookmark_empty_24px)
          ivAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
          dialog.dismiss()
        }, DialogInterface.OnClickListener { dialog, no ->
          dialog.dismiss()
        })
      }
    }

    ivFavourite.setOnClickListener {
      isFavourite = !isFavourite
      upsertFavourite()
      showSnackbar(if (isFavourite) getString(R.string.added_to_favourites) else getString(R.string.removed_from_favourites))
    }

    /*ivShare.setOnClickListener {
      whenNotNull(cache.getLastShownAyah()) {
        val shareText = "(${cache.getLastShownAyah()?.surahNumber}:${cache.getLastShownAyah()?.numberInSurah})" +
            " ${tvAyahTopText.text} *** ${tvAyahBottomText.text} ${getString(R.string.via_holy_signal)}"
        val sendIntent: Intent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, shareText)
          type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
      }
    }*/

    ivPlayMode.setOnClickListener {
      ayahSet.clear()
      val newPlayMode = (playmode + 1) % playmodes.size
      initPlaymode(newPlayMode)
      cache.updatePlaymode(newPlayMode)
      playmode = newPlayMode
      showSnackbar(playmodes[newPlayMode])
      ayahNumber = getAyahNumberByPlaymode()
      //initData()
    }

    ivAddNote.setOnClickListener {
      if (deviceUtil.isConnected()) {
        notesFragment = NotesFragment.newInstance(ayahNumber)
        notesFragment.show(supportFragmentManager, notesFragment.javaClass.simpleName)
      } else {
        showErrorDialog(getString(R.string.not_connected))
      }
    }

    ivSelectSurah.setOnClickListener {
      selectSurahFragment = SelectSurahFragment.newInstance()
      selectSurahFragment.show(supportFragmentManager, selectSurahFragment.javaClass.simpleName)
    }

    ivSearch.setOnClickListener {
      searchWordInAyahsFragment = SearchWordInAyahsFragment.newInstance()
      searchWordInAyahsFragment.show(supportFragmentManager, searchWordInAyahsFragment.javaClass.simpleName)
    }

//    tvNext.setOnClickListener {
//      ayahNumber = getAyahNumberByPlaymode()
//      initData()
//    }
//
//    tvPrevious.setOnClickListener {
//      ayahNumber = getAyahNumberByPlaymode(-1)
//      initData()
//    }

    ivSearchClose.setOnClickListener {
      cache.updateAyahSearchResult(null)
      tvNext.isEnabled = true
      ivPlayMode.visibility = View.VISIBLE
      ivSelectSurah.visibility = View.VISIBLE
      ivSearch.visibility = View.VISIBLE
      ivSearchClose.visibility = View.GONE
      tvKeywords.visibility = View.GONE
      ayahNumber = getAyahNumberByPlaymode()
      pageChanged()
    }

//    holyBookViewModel.totalFavouriteCount.observe(this, Observer<Int> {
//      //tvCloudFavouriteCount.text = getString(R.string.total_cloud_favourite_count, it)
//    })

    viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        pageChanged()
        if (position == ayahSet.size - 1) {
          populateAyahSet(playmode, position)
        }
      }
    })


  }

  private fun initPlaymode(mode: Int) {
    ivPlayMode.setImageResource(
        when (mode) {
          Playmode.RANDOM -> R.drawable.ic_random_24px
          Playmode.REPEAT_SURAH -> R.drawable.ic_repeat_surah24px
          Playmode.REPEAT_AYAH -> R.drawable.ic_repeat_ayah_24px
          Playmode.FAVOURITES -> R.drawable.ic_star_playlist_24px
          else -> R.drawable.ic_loop_24px
        }
    )
    tvNext.isEnabled = mode != Playmode.REPEAT_AYAH
    tvPrevious.isEnabled = (mode == Playmode.AYAH_BY_AYAH || mode == Playmode.REPEAT_SURAH)
  }


  private fun upsertFavourite() = runBlocking {
    var isAdd = 0
    holyBookViewModel.deleteFavourite(ayahNumber)
    if (isFavourite) {
      holyBookViewModel.insertFavourite(ayahNumber)
      isAdd = 1
    }
    holyBookViewModel.addFavouriteToCloud(ayahNumber, isAdd)
    holyBookViewModel.clearFavouriteCache()
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

  private fun getViewingPercentage() {
    lifecycleScope.launch {
      val count = holyBookViewModel.getTotalViewingCount()
      progress.progress = count
      tvProgress.text = getString(R.string.x_ayah_displayed, (count * 100) / ConstantVariables.MAX_AYAH_NUMBER)
    }
  }

//  override fun onSearchWordEntered(words: MutableSet<String>) {
//    try {
//      words.removeAll { it.trim() == "" }
//      if (words.isEmpty() || words.size > MAX_SEARCH_KEYWORD_THRESHOLD) {
//        showErrorSnackBar(R.string.ayah_search_validation_error)
//      } else {
//        lifecycleScope.launch {
//          val ayahNumbersBySearch: List<Int>? = holyBookViewModel.getAyahsByKeywords(cache.getTopTextEditionId(), words.toList())
//          val keywords = words.toString().removeBoxBracketsAndPutSpaceAfterComma()
//          val searchResult = AyahSearchResult(ayahNumbersBySearch, keywords)
//          cache.updateAyahSearchResult(searchResult)
//          arrangeViewsBySearch(searchResult)
//
//          if (ayahNumbersBySearch.isNotNullAndNotEmpty()) {
//            ayahNumber = getAyahNumberByPlaymode()
//            initData()
//          } else {
//            tvAyahNumber.setEmpty()
//            tvAyahTopText.setEmpty()
//            tvAyahBottomText.setEmpty()
//            tvViewingCount.setEmpty()
//          }
//        }
//      }
//    } catch (ex: Exception) {
//      showErrorSnackBar(ex.message!!)
//    } finally {
//      searchWordInAyahsFragment.dismiss()
//    }
//  }
//
//  override fun onSearchAyahNoEntered(ayahNo: Int) {
//    try {
//      if (ayahNo >= 1 && ayahNo <= cache.getMaxAyahCount()) {
//        arrangeViewsBySearch(AyahSearchResult(listOf(ayahNo), ayahNo.toString()))
//        ayahNumber = ayahNo
//        initData()
//      } else {
//        arrangeViewsBySearch(AyahSearchResult(null, ayahNo.toString()))
//        tvAyahNumber.setEmpty()
//        tvAyahTopText.setEmpty()
//        tvAyahBottomText.setEmpty()
//        tvViewingCount.setEmpty()
//      }
//    } catch (ex: Exception) {
//      showErrorSnackBar(ex.message!!)
//    } finally {
//      searchWordInAyahsFragment.dismiss()
//    }
//  }

  private fun arrangeViewsBySearch(searchResult: AyahSearchResult) {
    ivPlayMode.visibility = View.GONE
    ivSelectSurah.visibility = View.GONE
    ivSearch.visibility = View.GONE
    ivSearchClose.visibility = View.VISIBLE
    tvNext.isEnabled = searchResult.list.isNotNullAndNotEmpty()
    tvKeywords.visibility = View.VISIBLE
    tvKeywords.text = getString(R.string.ayah_search_found_text, searchResult.keywords, searchResult.list?.size
        ?: 0)
  }


  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(SAVED_AYAH_NUMBER, ayahNumber)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    savedInstanceState.getInt(SAVED_AYAH_NUMBER)
  }

  override fun onSurahSelected(surah: AvailableSurahItem) {
    ayahNumber = surah.min
    ayahSet.clear()
    populateAyahSet(playmode)
    selectSurahFragment.dismiss()
  }

  override fun onSearchWordEntered(words: MutableSet<String>) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onSearchAyahNoEntered(ayahNo: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


}

