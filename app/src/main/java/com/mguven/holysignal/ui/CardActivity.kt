package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
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
import com.mguven.holysignal.db.entity.SurahAyahSampleData
import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.extension.removeBoxBracketsAndPutSpaceAfterComma
import com.mguven.holysignal.extension.setEmpty
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.model.AyahMap
import com.mguven.holysignal.model.AyahSearchResult
import com.mguven.holysignal.ui.adapter.AyahViewPagerAdapter
import com.mguven.holysignal.ui.fragment.BaseDialogFragment
import com.mguven.holysignal.ui.fragment.NotesFragment
import com.mguven.holysignal.ui.fragment.SearchWordInAyahsFragment
import com.mguven.holysignal.ui.fragment.SelectSurahFragment
import com.mguven.holysignal.util.DepthPageTransformer
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random


class CardActivity : AbstractBaseActivity(),
    SearchWordInAyahsFragment.OnFragmentInteractionListener,
    SelectSurahFragment.OnFragmentInteractionListener,
    AyahViewPagerAdapter.MapValueListener {

  companion object {
    private const val MAX_SEARCH_KEYWORD_THRESHOLD = 3
    private const val AYAH_SET_MAX_SIZE = 5

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
  private var ayahMap = AyahMap()

  private var lastOpenedAyahNo = -100
  private var diffByPrevious = AYAH_SET_MAX_SIZE
  private var firstOpening = true
  private var isFavourite = false
  private var playmode: Int = Int.MIN_VALUE
  private val playmodes by lazy {
    return@lazy resources.getStringArray(R.array.playmodes)
  }

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

    arrangeCacheForOpening()

    viewpager.adapter = ayahViewPagerAdapter
    populateAyahSet(playmode)

    initListeners()
  }

  private fun arrangeCacheForOpening() {
    if (cache.getLastShownAyah() != null) {
      val isRepeatAyah = playmode == Playmode.REPEAT_AYAH
      val isAtLastItemOfRepeatSurah = (playmode == Playmode.REPEAT_SURAH && cache.getLastShownAyah()!!.endingAyahNumber == cache.getLastShownAyahNumber())

      if (!isRepeatAyah && !isAtLastItemOfRepeatSurah) {
        cache.arrangeAyahCacheForOpening()
      }
    }
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
            val ayahNumber = cache.getBookmark()
            lifecycleScope.launch {
              arrangeBySearchViewClosed()
              val ayah = holyBookViewModel.getAyahTopText(ayahNumber)
              cache.updateLastShownAyah(ayah[0])
              playmode = Playmode.REPEAT_AYAH
              ivPlayMode.performClick()
            }

            dialog.dismiss()
          }, DialogInterface.OnClickListener { dialog, no ->
            dialog.dismiss()
          })
        }
      }
    }

    ivBookMarkAyah.setOnClickListener {
      val bookmarkedAyah = cache.getBookmark()
      if (cache.getLastShownAyahNumber() != bookmarkedAyah) {
        showYesNoDialog(getString(if (bookmarkedAyah == ConstantVariables.EMPTY_BOOKMARK) R.string.new_bookmark_set_warning_message
        else R.string.bookmark_change_warning_message), DialogInterface.OnClickListener { dialog, yes ->
          cache.updateBookmark(cache.getLastShownAyahNumber())
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

    ivShare.setOnClickListener {
      whenNotNull(cache.getLastShownAyah()) {
        val shareText = "(${cache.getLastShownAyah()?.surahNumber}:${cache.getLastShownAyah()?.numberInSurah})" +
            " ${cache.getLastShownAyah()?.ayahText} ${getString(R.string.via_holy_signal)}"
        val sendIntent: Intent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, shareText)
          type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
      }
    }

    ivAddNote.setOnClickListener {
      if (deviceUtil.isConnected()) {
        notesFragment = NotesFragment.newInstance(cache.getLastShownAyahNumber())
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

    ivSearchClose.setOnClickListener {
      arrangeBySearchViewClosed()
      ivPlayMode.performClick()
    }

//    holyBookViewModel.totalFavouriteCount.observe(this, Observer<Int> {
//      //tvCloudFavouriteCount.text = getString(R.string.total_cloud_favourite_count, it)
//    })

    ivPlayMode.setOnClickListener {
      if (playmode == Playmode.SEARCH) ++playmode
      ayahMap.clear()
      val newPlayMode = onPlayModeChanged()
      populateAyahSet(newPlayMode)
      goToSelectedAyah()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      viewpager.setPageTransformer(DepthPageTransformer())
    }

    viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        firstOpening = false
      }

      override fun onPageSelected(pos: Int) {
        super.onPageSelected(pos)
        fun isFirstPage(position: Int) = position == 0
        fun isLastPage(position: Int) = position == ayahMap.size - 1
        fun canGoBack() = playmode == Playmode.AYAH_BY_AYAH || playmode == Playmode.REPEAT_SURAH
        fun canNotMove() = ayahMap.size == 1

        updateLastShownAyah(pos)
        getViewingPercentage()
        getViewingCount()

        onPageChanged()

        if (isFirstPage(pos)) {
          if (!firstOpening) {
            if (canGoBack()) {
              ayahMap.clear()
              if (!canNotMove()) {
                populateAyahSet(playmode)
              }
            }
          }
          if (canGoBack()) {
            goToSelectedAyah()
          }
        }

        if (isLastPage(pos)) {
          if (!firstOpening) {
            if (!canNotMove()) {
              populateAyahSet(playmode)
            }
          }
        }
      }
    })
  }

  private fun arrangeBySearchViewClosed() {
    cache.updateAyahSearchResult(null)
    tvNext.isEnabled = true
    ivPlayMode.visibility = View.VISIBLE
    ivSelectSurah.visibility = View.VISIBLE
    ivSearch.visibility = View.VISIBLE
    ivSearchClose.visibility = View.GONE
    tvKeywords.visibility = View.GONE
  }

  private fun onPlayModeChanged(newPlayMode: Int = (playmode + 1) % playmodes.size): Int {
    tvKeywords.visibility = View.GONE
    initPlaymode(newPlayMode)
    cache.updatePlaymode(newPlayMode)
    playmode = newPlayMode
    showSnackbar(playmodes[newPlayMode])
    return newPlayMode
  }

  private fun goToSelectedAyah() {
    viewpager.setCurrentItem(diffByPrevious, false)
  }

  private fun updateLastShownAyah(pos: Int) {
    val lastShownAyah = ayahMap.getValue(pos)
    whenNotNull(lastShownAyah) {
      cache.updateLastShownAyah(lastShownAyah)
    }
  }

  private fun populateAyahSet(playmode: Int) {
    val newAyahSet: Map<Int, SurahAyahSampleData?> = when (playmode) {
      Playmode.RANDOM -> populateAyahByRandom()
      Playmode.REPEAT_AYAH -> populateRepeatAyah()
      Playmode.AYAH_BY_AYAH -> populateAyahSetByAyahByAyah()
      Playmode.REPEAT_SURAH -> populateAyahSetByRepeatSurah()
      Playmode.FAVOURITES -> populateAyahsByFavourites()
      else -> {
        populateRepeatAyah()
      }
    }
    ayahMap.putAll(newAyahSet)

    updateAdapter()

    if (playmode == Playmode.RANDOM && !firstOpening) {
      viewpager.setCurrentItem(0, false)
    }
  }

  private fun populateBySearchResult() {
    ayahMap.clear()
    lifecycleScope.launch {
      val searchMap = cache.getAyahSearchResult()?.list!!.map { it }.associateWith { null }
      ayahMap.putAll(searchMap)
    }
    updateAdapter()
    viewpager.setCurrentItem(0, false)
  }

  private fun populateAyahsByFavourites(): Map<Int, SurahAyahSampleData?> {
    lifecycleScope.launch {
      val favouriteMap = holyBookViewModel.getFavouriteIdList()!!.map { it.toInt() }.associateWith { null }
      ayahMap.putAll(favouriteMap)
      return@launch
    }
    return ayahMap
  }

  private fun populateRepeatAyah(): Map<Int, SurahAyahSampleData?> {
    firstOpening = true
    return (1..1).map { cache.getLastShownAyahNumber() }.associateWith { null }
  }

  private fun populateAyahByRandom(): Map<Int, SurahAyahSampleData?> =
      List(AYAH_SET_MAX_SIZE) { Random.nextInt(1, cache.getMaxAyahCount()) }.map { it }.associateWith { null }

  private fun populateAyahSetByAyahByAyah(ayahNumber: Int = cache.getLastShownAyahNumber()): Map<Int, SurahAyahSampleData?> {
    val theMap = mutableMapOf<Int, SurahAyahSampleData?>()
    val margin = ayahNumber - 1
    diffByPrevious = if (margin >= AYAH_SET_MAX_SIZE) AYAH_SET_MAX_SIZE else margin
    val lowerLimit = ayahNumber - diffByPrevious
    val previousItemsMap = (ayahNumber downTo lowerLimit).reversed().map { it }.associateWith { null }
    theMap.putAll(previousItemsMap)

    val diff = ConstantVariables.MAX_AYAH_NUMBER - ayahNumber
    val addableItemCount = if (diff > AYAH_SET_MAX_SIZE) AYAH_SET_MAX_SIZE else diff
    val nextItemsMap = (ayahNumber..(ayahNumber + addableItemCount)).map { it }.associateWith { null }
    theMap.putAll(nextItemsMap)
    return theMap
  }

  private fun populateAyahSetByRepeatSurah(ayahNumber: Int = cache.getLastShownAyahNumber(),
                                           start: Int = cache.getLastShownAyah()!!.startingAyahNumber,
                                           end: Int = cache.getLastShownAyah()!!.endingAyahNumber): Map<Int, SurahAyahSampleData?> {
    val theMap = mutableMapOf<Int, SurahAyahSampleData?>()
    val margin = ayahNumber - start
    diffByPrevious = if (margin >= AYAH_SET_MAX_SIZE) AYAH_SET_MAX_SIZE else margin
    val lowerLimit = ayahNumber - diffByPrevious
    val previousItemsMap = (ayahNumber downTo lowerLimit).reversed().map { it }.associateWith { null }
    theMap.putAll(previousItemsMap)

    val diff = end - ayahNumber
    val addableItemCount = if (diff > AYAH_SET_MAX_SIZE) AYAH_SET_MAX_SIZE else diff
    val nextItemsMap = (ayahNumber..(ayahNumber + addableItemCount)).map { it }.associateWith { null }
    theMap.putAll(nextItemsMap)
    return theMap
  }

  override fun onSurahSelected(surah: AvailableSurahItem) {
    onPlayModeChanged(Playmode.REPEAT_SURAH)
    ayahMap.clear()
    val next = populateAyahSetByRepeatSurah(surah.min, surah.min, surah.max)
    ayahMap.putAll(next)
    cache.updateLastShownAyah(null)
    updateAdapter()
    firstOpening = true
    viewpager.setCurrentItem(0, false)
    selectSurahFragment.dismiss()
  }

  private fun updateAdapter() {
    if (ayahMap.isEmpty()) {
      viewpager.visibility = View.GONE
      tvEmptyMessage.text = getString(R.string.none_items_found)
      tvEmptyMessage.visibility = View.VISIBLE
      tvViewingCount.visibility = View.GONE
      ivShare.visibility = View.GONE
      ivFavourite.visibility = View.GONE
      ivAddNote.visibility = View.GONE
      ivBookMarkAyah.visibility = View.GONE
    } else {
      viewpager.visibility = View.VISIBLE
      tvEmptyMessage.visibility = View.GONE
      tvViewingCount.visibility = View.VISIBLE
      ivShare.visibility = View.VISIBLE
      ivFavourite.visibility = View.VISIBLE
      ivAddNote.visibility = View.VISIBLE
      ivBookMarkAyah.visibility = View.VISIBLE
      ayahViewPagerAdapter.updateAyahSet(ayahMap)
    }
    Log.e("AYAH_SET", "playmode: $playmode  mapSize: ${ayahMap.size} map: $ayahMap")
  }

  private fun getViewingCount() {
    lifecycleScope.launch {
      val count = holyBookViewModel.getViewingCount(cache.getLastShownAyahNumber())
      tvViewingCount.text = tvViewingCount.context.getString(R.string.total_viewing_count, count)
    }
  }

  private fun onPageChanged() {
    Log.e("AYAH_SET", "PAGE CHANGED -- ayahNumber: ${cache.getLastShownAyahNumber()}")
    if (lastOpenedAyahNo != cache.getLastShownAyahNumber()) {
      //holyBookViewModel.getFavouriteCountByAyahNumber(ayahNumber)
      ivFavourite.visibility = View.VISIBLE
      ivBookMarkAyah.setImageResource(if (cache.getBookmark() == cache.getLastShownAyahNumber()) R.drawable.ic_bookmark_filled_24px else R.drawable.ic_bookmark_empty_24px)
      ivAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE

      showFavouriteStatus()
      lastOpenedAyahNo = cache.getLastShownAyahNumber()
    }
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
    holyBookViewModel.deleteFavourite(cache.getLastShownAyahNumber())
    if (isFavourite) {
      holyBookViewModel.insertFavourite(cache.getLastShownAyahNumber())
      isAdd = 1
    }
    holyBookViewModel.addFavouriteToCloud(cache.getLastShownAyahNumber(), isAdd)
    holyBookViewModel.clearFavouriteCache()
    showFavouriteStatus()
  }

  private fun showFavouriteStatus() {
    lifecycleScope.launch(Dispatchers.IO) {
      holyBookViewModel.hasFavourite(cache.getLastShownAyahNumber()).also { list ->
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

  override fun onSearchWordEntered(words: MutableSet<String>) {
    try {
      playmode = Playmode.FAVOURITES
      words.removeAll { it.trim() == "" }
      if (words.isEmpty() || words.size > MAX_SEARCH_KEYWORD_THRESHOLD) {
        showErrorSnackBar(R.string.ayah_search_validation_error)
      } else {
        lifecycleScope.launch {
          val ayahNumbersBySearch: List<Int>? = holyBookViewModel.getAyahsByKeywords(cache.getTopTextEditionId(), words.toList())
          val keywords = words.toString().removeBoxBracketsAndPutSpaceAfterComma()
          val searchResult = AyahSearchResult(ayahNumbersBySearch, keywords)
          cache.updateAyahSearchResult(searchResult)
          arrangeViewsBySearch(searchResult)
          if (ayahNumbersBySearch.isNotNullAndNotEmpty()) {
            populateBySearchResult()
            viewpager.visibility = View.VISIBLE
            tvEmptyMessage.visibility = View.GONE
          } else {
            viewpager.visibility = View.GONE
            tvEmptyMessage.visibility = View.VISIBLE
            tvEmptyMessage.text = getString(R.string.none_items_found)
            tvViewingCount.setEmpty()
          }
        }
      }
    } catch (ex: Exception) {
      showErrorSnackBar(ex.message!!)
    } finally {
      searchWordInAyahsFragment.dismiss()
    }
  }

  override fun onSearchAyahNoEntered(ayahNo: Int) {
    try {
      playmode = Playmode.FAVOURITES
      if (ayahNo >= 1 && ayahNo <= cache.getMaxAyahCount()) {
        val searchResult = AyahSearchResult(listOf(ayahNo), ayahNo.toString())
        cache.updateAyahSearchResult(searchResult)
        arrangeViewsBySearch(searchResult)
        populateBySearchResult()
        viewpager.visibility = View.VISIBLE
        tvEmptyMessage.visibility = View.GONE
      } else {
        arrangeViewsBySearch(AyahSearchResult(null, ayahNo.toString()))
        viewpager.visibility = View.GONE
        tvEmptyMessage.visibility = View.VISIBLE
        tvEmptyMessage.text = getString(R.string.none_items_found)
        tvViewingCount.setEmpty()
      }
    } catch (ex: Exception) {
      showErrorSnackBar(ex.message!!)
    } finally {
      searchWordInAyahsFragment.dismiss()
    }
  }

  override fun onMapValueFound(ayahMap: AyahMap) {
    this.ayahMap = ayahMap
    ayahMap.forEach {
      if (it.value != null) {
        Log.e("AYAH_SET", "MAPVALUEFOUND -- ayahNumber: ${it.value}")
        cache.updateLastShownAyah(it.value)
        //onPageChanged()
        return@forEach
      }
    }

  }

}

