package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
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
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random


class CardActivity : AbstractBaseActivity(),
    SearchWordInAyahsFragment.OnFragmentInteractionListener,
    SelectSurahFragment.OnFragmentInteractionListener,
    AyahViewPagerAdapter.MapValueListener,
    DeviceUtil.AudioListener {

  companion object {
    private const val MAX_SEARCH_KEYWORD_THRESHOLD = 3
    private const val AYAH_SET_MAX_SIZE = 5
    private const val AUTO_MODE_LEVEL_MAX = 3
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

  private var future: ScheduledFuture<*>? = null
  private val scheduledExecutor by lazy {
    return@lazy Executors.newSingleThreadScheduledExecutor()
  }
  private var wordCount = 0
  private var pagePosition = -1

  private var lastOpenedAyahNo = -100
  private var diffByPrevious = AYAH_SET_MAX_SIZE
  private var firstOpening = true
  private var isFavourite = false
  private var playmode: Int = Int.MIN_VALUE
  private val playmodes by lazy {
    return@lazy resources.getStringArray(R.array.playmodes)
  }
  private var autoModeLevel = 0

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
    updateFirstAyahIfLastShownAyahIsNull()
    deviceUtil.audioListener = this
    initAdMob()
    if(!cache.isSpotlightShown()){
      Handler().postDelayed({
        showSpotlight()
        cache.updateSpotlightShownState(true)
      }, 1500)
    }
  }

  private fun initAdMob() {
    if (cache.showAdvertisement()) {
      flDownload.visibility = View.GONE
      adBanner.visibility = View.VISIBLE
      MobileAds.initialize(adBanner.context) {}
      val adRequest = AdRequest.Builder().build()
      adBanner.loadAd(adRequest)
    } else {
      flDownload.visibility = View.VISIBLE
      adBanner.visibility = View.GONE
    }
  }

  private fun autoModeOff() {
    autoModeLevel = 0
    ivAutoMode.setImageResource(R.drawable.ic_play_arrow_24px)
    progressAutoMode.visibility = View.GONE
    progressAutoMode.progress = 0
  }

  private fun incrementProgressAutoMode() {
    if (future != null) {
      future!!.cancel(true)
    }

    Log.e("AAA", "incrementProgressAutoMode 1")
    if (cache.getLastShownAyah() == null) {
      Handler().postDelayed({
        incrementProgressAutoMode()
      }, 1000)
    }

    Log.e("AAA", "incrementProgressAutoMode 2")
    future = scheduledExecutor.schedule({
      runOnUiThread {
        progressAutoMode.visibility = View.VISIBLE
        progressAutoMode.progress = progressAutoMode.progress + (10 * (autoModeLevel))
        if (progressAutoMode.progress >= wordCount * 10) {
          if (ayahMap.size - 1 == viewpager.currentItem) {
            Log.e("AAA", "incrementProgressAutoMode 3")
            autoModeOff()
          } else {
            Log.e("AAA", "incrementProgressAutoMode 4")
            progressAutoMode.progress = 0
            viewpager.setCurrentItem(viewpager.currentItem + 1, true)
          }
        } else {
          if ((autoModeLevel % AUTO_MODE_LEVEL_MAX) != 0) {
            Log.e("AAA", "incrementProgressAutoMode 5")
            incrementProgressAutoMode()
          } else {
            Log.e("AAA", "incrementProgressAutoMode 6")
            autoModeOff()
          }
        }
      }
    }, 1, TimeUnit.SECONDS)

  }

  private fun autoModeOnlyViewChange() {
    ivAutoMode.setImageResource(if (autoModeLevel == 1) R.drawable.ic_fast_forward_24px else R.drawable.ic_stop_24px)

    val color = if (autoModeLevel == 1) R.color.button_green else R.color.orange
    val mode = PorterDuff.Mode.SRC_IN
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      progressAutoMode.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))
      progressAutoMode.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))
    } else {
      val progressDrawable = (if (progressAutoMode.isIndeterminate) progressAutoMode.indeterminateDrawable else progressAutoMode.progressDrawable).mutate()
      progressDrawable.setColorFilter(ContextCompat.getColor(this, color), mode)
      progressAutoMode.progressDrawable = progressDrawable
    }

    progressAutoMode.visibility = View.VISIBLE
    progressAutoMode.progress = 0
    incrementProgressAutoMode()
  }

  private fun showSpotlight() {
    val targets = ArrayList<Target>()
    val firstTarget = makeTarget(targetViewId = R.id.ivSearch, descriptionId = R.string.spotlight_layout_1_text)
    targets.add(firstTarget)

    val secondTarget = makeTarget(targetViewId = R.id.ivBookMarkAyah, descriptionId = R.string.spotlight_layout_2_text)
    targets.add(secondTarget)

    val thirdTarget = makeTarget(targetViewId = R.id.ivSelectSurah, descriptionId = R.string.spotlight_layout_3_text)
    targets.add(thirdTarget)

    val fourthTarget = makeTarget(targetViewId = R.id.ivPlayMode, descriptionId = R.string.spotlight_layout_4_text)
    targets.add(fourthTarget)

    val fifthTarget = makeTarget(targetViewId = R.id.progress, descriptionId = R.string.spotlight_layout_5_text)
    targets.add(fifthTarget)

    val sixthTarget = makeTarget(targetViewId = R.id.ivPreferences, descriptionId = R.string.spotlight_layout_6_text)
    targets.add(sixthTarget)

    val twelfthTarget = makeTarget(targetViewId = R.id.ivAutoMode, descriptionId = R.string.spotlight_layout_12_text)
    targets.add(twelfthTarget)

    val eleventhTarget = makeTarget(targetViewId = R.id.ivAudio, descriptionId = R.string.spotlight_layout_11_text)
    targets.add(eleventhTarget)

    val seventhTarget = makeTarget(targetViewId = R.id.ivShare, descriptionId = R.string.spotlight_layout_7_text)
    targets.add(seventhTarget)

    val eightTarget = makeTarget(targetViewId = R.id.ivFavourite, descriptionId = R.string.spotlight_layout_8_text)
    targets.add(eightTarget)

    val ninthTarget = makeTarget(targetViewId = R.id.ivAddNote, descriptionId = R.string.spotlight_layout_9_text)
    targets.add(ninthTarget)

    val tenthTarget = makeTarget(targetViewId = R.id.tvViewingCount, descriptionId = R.string.spotlight_layout_10_text, radius = 200f)
    targets.add(tenthTarget)

    val thirteenthTarget = makeTarget(layoutId = R.layout.layout_target_2, targetViewId = R.id.scrollview, descriptionId = R.string.spotlight_layout_13_text, radius = 500f)
    targets.add(thirteenthTarget)


    val spotlight = Spotlight.Builder(this@CardActivity)
        .setTargets(targets)
        .setBackgroundColor(R.color.spotlight_background)
        .setDuration(500L)
        .setAnimation(DecelerateInterpolator(2f))
        .build()

    spotlight.start()

    val nextTarget = View.OnClickListener { spotlight.next() }
    val closeSpotlight = View.OnClickListener { spotlight.finish() }

    firstTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    secondTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    thirdTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    fourthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    fifthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    sixthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    seventhTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    eightTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    ninthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    tenthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    eleventhTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    twelfthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)
    thirteenthTarget.overlay?.findViewById<View>(R.id.next_target)?.setOnClickListener(nextTarget)

    firstTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    secondTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    thirdTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    fourthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    fifthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    sixthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    seventhTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    eightTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    ninthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    tenthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    eleventhTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)
    twelfthTarget.overlay?.findViewById<View>(R.id.close_target)?.setOnClickListener(closeSpotlight)

  }

  private fun makeTarget(layoutId: Int = R.layout.layout_target_1, targetViewId: Int, descriptionId: Int, radius: Float = 100f): Target {
    val theRoot = FrameLayout(this)
    val theLayout = layoutInflater.inflate(layoutId, theRoot)
    theLayout.findViewById<TextView>(R.id.tvDescription).text = getString(descriptionId)
    return Target.Builder()
        .setAnchor(findViewById<View>(targetViewId))
        .setShape(Circle(radius))
        .setOverlay(theLayout)
        .build()
  }

  private fun updateFirstAyahIfLastShownAyahIsNull() {
    if (cache.getLastShownAyah() == null) {
      lifecycleScope.launch {
        val ayah = holyBookViewModel.getAyahTopText(1)
        cache.updateLastShownAyah(ayah[0])
        updateFirstAyahIfLastShownAyahIsNull()
      }
    } else {
      playmode = cache.getPlaymode()
      initPlaymode(playmode)
      arrangeCacheForOpening()
      viewpager.adapter = ayahViewPagerAdapter
      populateAyahSet(playmode)
      initListeners()
      cache.updateAyahSearchResult(null)
    }
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
    ivHelp.setOnClickListener {
      stopAutoMode()
      showSpotlight()
    }

    ivAudio.setOnClickListener {
      deviceUtil.playAudio(ConstantVariables.getAudioUrl(cache.getLastShownAyahNumber()))
    }

    ivAutoMode.setOnClickListener {
      autoModeLevel = (autoModeLevel + 1) % AUTO_MODE_LEVEL_MAX
      arrangeAutoModeLevel()
    }

    ivPreferences.setOnClickListener {
      FlowController.launchMainActivity(this)
      ivPreferences.isEnabled = false
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
          flAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
          dialog.dismiss()
        }, DialogInterface.OnClickListener { dialog, no ->
          dialog.dismiss()
        })
      } else {
        showYesNoDialog(getString(R.string.remove_bookmark_warning_message), DialogInterface.OnClickListener { dialog, yes ->
          cache.updateBookmark(ConstantVariables.EMPTY_BOOKMARK)
          ivBookMarkAyah.setImageResource(R.drawable.ic_bookmark_empty_24px)
          flAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
          dialog.dismiss()
        }, DialogInterface.OnClickListener { dialog, no ->
          dialog.dismiss()
        })
      }
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
        stopAutoMode()
        notesFragment = NotesFragment.newInstance(cache.getLastShownAyahNumber())
        notesFragment.show(supportFragmentManager, notesFragment.javaClass.simpleName)
      } else {
        showErrorDialog(getString(R.string.not_connected))
      }
    }

    ivSelectSurah.setOnClickListener {
      stopAutoMode()
      selectSurahFragment = SelectSurahFragment.newInstance()
      selectSurahFragment.show(supportFragmentManager, selectSurahFragment.javaClass.simpleName)
    }

    ivSearch.setOnClickListener {
      stopAutoMode()
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

    viewpager.setPageTransformer(DepthPageTransformer())


    flDownload.setOnClickListener {
      FlowController.launchDownloadActivity(this)
    }


    viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        firstOpening = false
      }

      override fun onPageSelected(pos: Int) {
        pagePosition = pos
        super.onPageSelected(pos)
        fun isFirstPage(position: Int) = position == 0
        fun isLastPage(position: Int) = position == ayahMap.size - 1
        fun canGoBack() = playmode == Playmode.AYAH_BY_AYAH || playmode == Playmode.REPEAT_SURAH
        fun canNotMove() = ayahMap.size == 1

        updateLastShownAyah()
        refreshPageValues()

        if (!firstOpening) {
          deviceUtil.stopAudio()
        }

        if (isFirstPage(pos)) {
          if (canGoBack()) {
            if (!firstOpening) {
              ayahMap.clear()
              if (!canNotMove()) {
                populateAyahSet(playmode)
              }
            }
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
        arrangeAutoModeLevel()
      }
    })
  }

  private fun arrangeAutoModeLevel() {
    when (autoModeLevel) {
      -1, 0 -> autoModeOff()
      else -> autoModeOnlyViewChange()
    }
  }

  private fun refreshPageValues() {
    if (lastOpenedAyahNo != cache.getLastShownAyahNumber()) {
      Log.e("REFRESH PAGE", "PAGE CHANGED -- ayahNumber: ${cache.getLastShownAyahNumber()}")
      getViewingPercentage()
      getViewingCount()
      showFavouriteStatus()
      onPageChanged()
    }
  }

  private fun arrangeBySearchViewClosed() {
    cache.updateAyahSearchResult(null)
    ivPlayMode.visibility = View.VISIBLE
    ivSelectSurah.visibility = View.VISIBLE
    ivSearch.visibility = View.VISIBLE
    ivSearchClose.visibility = View.GONE
    tvKeywords.visibility = View.GONE
    flHelp.visibility = View.VISIBLE
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

  private fun updateLastShownAyah() {
    val lastShownAyah = ayahMap.getValue(viewpager.currentItem)
    cache.updateLastShownAyah(lastShownAyah)
    if (lastShownAyah != null) {
      wordCount = lastShownAyah.ayahText.split(" ").size
      progressAutoMode.max = wordCount * 10
    }
    Log.e("SELECTED_SURAH", lastShownAyah?.numberInSurah.toString())
  }

  private fun populateAyahSet(playmode: Int) {
    try {
      val newAyahSet: Map<Int, SurahAyahSampleData?> = when (playmode) {
        Playmode.RANDOM -> populateAyahByRandom()
        Playmode.REPEAT_AYAH -> populateRepeatAyah()
        Playmode.AYAH_BY_AYAH -> populateAyahSetByAyahByAyah()
        Playmode.REPEAT_SURAH -> {
          val lastShownAyah = cache.getLastShownAyah()!!
          populateAyahSetByRepeatSurah(lastShownAyah.ayahNumber,
              lastShownAyah.startingAyahNumber,
              lastShownAyah.endingAyahNumber)
        }
        Playmode.FAVOURITES -> populateAyahsByFavourites()
        Playmode.SEARCH -> populateBySearchResult()
        else -> {
          populateRepeatAyah()
        }
      }

      ayahMap.putAll(newAyahSet)

      updateAdapter()
    } catch (ex: Exception) {
      ayahMap.clear()
      updateAdapter()
    }
  }

  private fun populateBySearchResult(): Map<Int, SurahAyahSampleData?> {
    ayahMap.clear()
    ayahViewPagerAdapter.updateAyahSet(ayahMap)
    lifecycleScope.launch {
      val searchMap = cache.getAyahSearchResult()?.list!!.map { it }.associateWith { null }
      ayahMap.putAll(searchMap)
    }
    return ayahMap
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

  private fun populateAyahSetByRepeatSurah(ayahNumber: Int,
                                           start: Int,
                                           end: Int): Map<Int, SurahAyahSampleData?> {
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
      ivAutoMode.visibility = View.GONE
      ivHelp.visibility = View.GONE
      ivAudio.visibility = View.GONE
    } else {
      viewpager.visibility = View.VISIBLE
      tvEmptyMessage.visibility = View.GONE
      tvViewingCount.visibility = View.VISIBLE
      ivShare.visibility = View.VISIBLE
      ivFavourite.visibility = View.VISIBLE
      ivAddNote.visibility = View.VISIBLE
      ivBookMarkAyah.visibility = View.VISIBLE
      ivAutoMode.visibility = View.VISIBLE
      ivHelp.visibility = View.VISIBLE
      ivAudio.visibility = View.VISIBLE
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
    //holyBookViewModel.getFavouriteCountByAyahNumber(ayahNumber)
    ivFavourite.visibility = View.VISIBLE
    ivBookMarkAyah.setImageResource(if (cache.getBookmark() == cache.getLastShownAyahNumber()) R.drawable.ic_bookmark_filled_24px else R.drawable.ic_bookmark_empty_24px)
    flAllBookmarks.visibility = if (cache.getBookmark() == ConstantVariables.EMPTY_BOOKMARK) View.GONE else View.VISIBLE
    lastOpenedAyahNo = cache.getLastShownAyahNumber()

  }

  private fun initPlaymode(mode: Int) {
    ivPlayMode.setImageResource(
        when (mode) {
          Playmode.RANDOM -> R.drawable.ic_random_24px
          Playmode.REPEAT_SURAH -> R.drawable.ic_repeat_surah24px
          Playmode.REPEAT_AYAH -> R.drawable.ic_repeat_ayah_24px
          Playmode.FAVOURITES -> R.drawable.ic_heart
          else -> R.drawable.ic_loop_24px
        }
    )
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
        ivFavourite.setImageResource(if (isFavourite) R.drawable.ic_heart_fill else R.drawable.ic_heart)
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
    ivBookMarkAyah.visibility = if (searchResult.list.isNotNullAndNotEmpty()) View.VISIBLE else View.GONE
    tvKeywords.visibility = View.VISIBLE
    tvKeywords.text = getString(R.string.ayah_search_found_text, searchResult.keywords, searchResult.list?.size
        ?: 0)
    flHelp.visibility = View.GONE
  }

  override fun onSearchWordEntered(words: MutableSet<String>) {
    try {
      playmode = Playmode.SEARCH
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
            viewpager.setCurrentItem(0, false)
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
      playmode = Playmode.SEARCH
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
    updateLastShownAyah()
    refreshPageValues()
  }

  override fun onAyahClicked() {
    isFavourite = !isFavourite
    playHeartAnimation()
    upsertFavourite()
  }

  private fun playHeartAnimation() {
    ivHeart.alpha = 0.9f
    if (ivHeart.drawable is AnimatedVectorDrawableCompat) {
      ivHeart.visibility = View.VISIBLE
      (ivHeart.drawable as AnimatedVectorDrawableCompat).start()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ivHeart.drawable is AnimatedVectorDrawable) {
      ivHeart.visibility = View.VISIBLE
      (ivHeart.drawable as AnimatedVectorDrawable).start()
    } else {
      ivHeart.visibility = View.GONE
    }
  }

  override fun onAyahLongPressed() {
    ivBookMarkAyah.performClick()
  }

  override fun onAudioFinished() {
    ivAudio.setImageResource(R.drawable.ic_voice_over_off_24px)
  }

  override fun onAudioStarted() {
    ivAudio.setImageResource(R.drawable.ic_record_voice_over_24px)
  }

  override fun onAudioWaiting() {
    ivAudio.setImageResource(R.drawable.ic_import_export_24px)
  }

  override fun onPause() {
    super.onPause()
    deviceUtil.stopAudio()
    stopAutoMode()
  }

  private fun stopAutoMode() {
    autoModeLevel = 0
    arrangeAutoModeLevel()
  }

  override fun onResume() {
    super.onResume()
    ivPreferences.isEnabled = true
  }

}

