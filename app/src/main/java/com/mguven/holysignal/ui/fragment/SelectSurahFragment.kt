package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.db.entity.AvailableSurahItem
import com.mguven.holysignal.di.module.SelectSurahFragmentModule
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.adapter.SearchableSpinnerAdapter
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.fragment_select_surah.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class SelectSurahFragment : BaseDialogFragment() {

  @Inject
  lateinit var cache: ApplicationCache

  override fun getLayoutResource() = R.layout.fragment_select_surah
  private var availableSurahList: List<AvailableSurahItem>? = null

  private lateinit var holyBookViewModel: HolyBookViewModel

  private var listener: OnFragmentInteractionListener? = null

  companion object {
    fun newInstance() = SelectSurahFragment().apply {
    }
  }

  private fun inject(application: TheApplication) {
    application
        .applicationComponent
        .plus(SelectSurahFragmentModule(this))
        .inject(this)
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
    }

    btnGo.setOnClickListener {
      val position = searchableSpinner.selectedItemPosition
      listener?.onSurahSelected(availableSurahList!![position])
    }

    fillSurahList()

  }

  private fun fillSurahList() {
    searchableSpinner.setTitle(getString(R.string.select_surah))
    searchableSpinner.setPositiveButton(getString(R.string.ok))

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

  private fun updateAvailableSurahListAdapter(list: List<AvailableSurahItem>) {
    var selectedItem = 0
    list.forEachIndexed { index, it ->
      if (it.value == cache.getLastShownAyah()?.surahNumber) {
        selectedItem = index
        return@forEachIndexed
      }
    }

    updateAdapter(list, selectedItem)

  }

  private fun updateAdapter(list: List<AvailableSurahItem>, selectedItem: Int = 0) {
    searchableSpinner.adapter = SearchableSpinnerAdapter(activity as Context, R.layout.status_item, list.map { it.key }, selectedItem)
    touchProgrammatically()
  }

  private fun touchProgrammatically() {
    val downTime = SystemClock.uptimeMillis()
    val eventTime = SystemClock.uptimeMillis() + 100
    val x = 0.0f
    val y = 0.0f
    val metaState = 0
    val motionEvent = MotionEvent.obtain(
        downTime,
        eventTime,
        MotionEvent.ACTION_UP,
        x,
        y,
        metaState
    )
    searchableSpinner!!.dispatchTouchEvent(motionEvent)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      listener = context
      inject(context.applicationContext as TheApplication)
    } else {
      throw RuntimeException("$context must be implement OnFragmentInteractionListener")
    }
  }


  interface OnFragmentInteractionListener {
    fun onSurahSelected(surah: AvailableSurahItem)
  }
}