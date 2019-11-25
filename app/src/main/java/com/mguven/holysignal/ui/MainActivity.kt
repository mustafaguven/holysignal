package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.job.LockScreenJob
import com.mguven.holysignal.ui.adapter.EditionAdapter
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch


class MainActivity : AbstractBaseActivity() {

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    inject(MainActivityModule(this))
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    runJobScheduler()

    initEditionSpinners()

    btnOk.setOnClickListener {
      val topTextEditionSpinnerSelectedItem = spTopTextEdition.selectedItem as EditionAdapterData
      val bottomTextEditionSpinnerSelectedItem = spBottomTextEdition.selectedItem as EditionAdapterData
      cache.updateTopTextEditionId(topTextEditionSpinnerSelectedItem.value)
      cache.updateBottomTextEditionId(bottomTextEditionSpinnerSelectedItem.value)
      getMaxAyahCount()
      Toast.makeText(this, getString(R.string.preferences_saved), Toast.LENGTH_SHORT).show()
    }

    btnDownloadTop.setOnClickListener {
      val downloadableSelectedItem = spTopTextEdition.selectedItem as EditionAdapterData
      val editionId = downloadableSelectedItem.value
      preferencesViewModel.downloadSurahTranslatedNames(editionId)
      preferencesViewModel.downloadSurah(editionId)
    }

    cache.downloadedSurah.observe(this, Observer<Int> {
      it?.let {
        progressTop.max = ConstantVariables.MAX_SURAH_NUMBER
        progressTop.progress = it
        tvProgressTextTop.text = if (it == ConstantVariables.MAX_SURAH_NUMBER)
          getString(R.string.download_finished)
        else
          calculatePercentage(R.string.surah_ayahs_is_downloading, it, ConstantVariables.MAX_SURAH_NUMBER)
        val isDone = it == ConstantVariables.MAX_SURAH_NUMBER
        btnDownloadTop.visibility = if (isDone) View.VISIBLE else View.INVISIBLE
        progressTop.visibility = if (isDone) View.GONE else View.VISIBLE
        tvProgressTextTop.visibility = if (isDone) View.GONE else View.VISIBLE
      }
    })

    cache.downloadedSurahTranslate.observe(this, Observer<IntArray> {
      btnDownloadTop.visibility = View.INVISIBLE
      it?.let {
        progressTop.max = it[0]
        val isDone = (it[0]) == it[1]
        progressTop.progress = it[1]
        tvProgressTextTop.text = if (isDone)
          getString(R.string.download_finished)
        else
          calculatePercentage(R.string.surah_translate_is_downloading, it[1], it[0])
        progressTop.visibility = if (isDone) View.GONE else View.VISIBLE
        tvProgressTextTop.visibility = if (isDone) View.GONE else View.VISIBLE
        Log.e("BBB", "${(it[0])} -- ${it[1]}")
      }
    })
  }

  private fun calculatePercentage(res: Int, number: Int, total: Int): String = "${((number * 100) / total)}%"

  private fun initEditionSpinners() {
    lifecycleScope.launch {
      val list = preferencesViewModel.getEditionNameIdList()
      val res = resources
      val adapter = EditionAdapter(this@MainActivity, R.layout.status_item, list, res)
      spTopTextEdition.adapter = adapter
      list.forEachIndexed { index, it ->
        if (it.value == cache.getTopTextEditionId()) {
          spTopTextEdition.setSelection(index)
          return@forEachIndexed
        }
      }

      spBottomTextEdition.adapter = adapter
      list.forEachIndexed { index, it ->
        if (it.value == cache.getBottomTextEditionId()) {
          spBottomTextEdition.setSelection(index)
          return@forEachIndexed
        }
      }
    }
  }

  private fun getMaxAyahCount() {
    lifecycleScope.launch {
      val result = preferencesViewModel.getMaxAyahCount()
      Log.e("AAA", "=====> MAX AYAH COUNT ${result.max}")
      cache.updateMaxAyahCount(result.max)
      finish()
    }
  }

  override fun onStop() {
    super.onStop()
    cancelImmediateJobScheduler()
  }

  private fun runJobScheduler() {
    var jobSets_I: MutableSet<JobRequest>? = null
    var jobSets_P: MutableSet<JobRequest>? = null
    try {
      jobSets_I = JobManager.instance().getAllJobRequestsForTag(LockScreenJob.TAG_I)
      jobSets_P = JobManager.instance().getAllJobRequestsForTag(LockScreenJob.TAG_P)

      if (jobSets_I == null || jobSets_I.isEmpty()) {
        LockScreenJob.runJobImmediately()
      }
      if (jobSets_P == null || jobSets_P.isEmpty()) {
        LockScreenJob.scheduleJobPeriodic()
      }

      //Cancel pending job scheduler if mutiple instance are running.
      if (jobSets_P != null && jobSets_P.size > 2) {
        JobManager.instance().cancelAllForTag(LockScreenJob.TAG_P)
      }
    } catch (e: Exception) {

      e.printStackTrace()

    } finally {
      jobSets_I?.clear()
      jobSets_P?.clear()
    }
  }

  /**
   * cancelImmediateJobScheduler: cancel all instance of running job scheduler by their
   * TAG name.
   */
  private fun cancelImmediateJobScheduler() {
    JobManager.instance().cancelAllForTag(LockScreenJob.TAG_I)
  }


}

