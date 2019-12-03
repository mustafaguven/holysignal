package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.job.LockScreenJob
import com.mguven.holysignal.ui.adapter.EditionAdapter
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loadingprogress.*
import kotlinx.coroutines.launch


class MainActivity : AbstractBaseActivity() {

  private lateinit var preferencesViewModel: PreferencesViewModel
  var membershipState = ConstantVariables.MEMBER_IS_NOT_FOUND

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    inject(MainActivityModule(this))
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)
    cbActivePassive.isChecked = cache.isActive()
    cbActivePassive.text = if (cbActivePassive.isChecked) {
      getString(R.string.active)
    } else {
      getString(R.string.passive)
    }
    cbAltTextActivePassive.isChecked = cache.hasSecondLanguageSupport()
    clAlternateText.visibility = if (cbAltTextActivePassive.isChecked) View.VISIBLE else View.GONE

    runJobScheduler()

    initEditionSpinners()

    btnOk.setOnClickListener {
      val topTextEditionSpinnerSelectedItem = spTopTextEdition.selectedItem as EditionAdapterData
      val bottomTextEditionSpinnerSelectedItem = spBottomTextEdition.selectedItem as EditionAdapterData
      cache.updateTopTextEditionId(topTextEditionSpinnerSelectedItem.value)
      cache.updateBottomTextEditionId(bottomTextEditionSpinnerSelectedItem.value)
      updateMaxAyahCount()
      FlowController.launchCardActivity(this, true)
      Toast.makeText(this, getString(R.string.preferences_saved), Toast.LENGTH_SHORT).show()
    }

    btnDownloadTop.setOnClickListener {
      onClickDownloadButton(spTopTextEdition, ConstantVariables.TOP_TEXT)
    }

    btnDownloadBottom.setOnClickListener {
      onClickDownloadButton(spBottomTextEdition, ConstantVariables.BOTTOM_TEXT)
    }

    cache.downloadedTopSurahTranslate.observe(this, Observer<IntArray> {
      percentageSurahTranslate(it, progressTop, tvProgressTextTop, btnDownloadTop)
    })

    cache.downloadedBottomSurahTranslate.observe(this, Observer<IntArray> {
      percentageSurahTranslate(it, progressBottom, tvProgressTextBottom, btnDownloadBottom)
    })

    cache.downloadedTopSurah.observe(this, Observer<Int> {
      percentageDownload(it, progressTop, tvProgressTextTop, btnDownloadTop)
    })

    cache.downloadedBottomSurah.observe(this, Observer<Int> {
      percentageDownload(it, progressBottom, tvProgressTextBottom, btnDownloadBottom)
    })

    preferencesViewModel.isMember.observe(this, Observer<Int> {
      prepareScreenByMembership(it)
    })

    tvLoginMessage.setOnClickListener {
      openLoginActivity()
    }

    cbActivePassive.setOnClickListener {
      cache.updateActivePassive(cbActivePassive.isChecked)
      Log.e("AAA", cache.isActive().toString())
      cbActivePassive.text = if (cbActivePassive.isChecked) {
        getString(R.string.active)
      } else {
        getString(R.string.passive)
      }
    }

    cbAltTextActivePassive.setOnClickListener {
      cache.updateSecondLanguageSupport(cbAltTextActivePassive.isChecked)
      clAlternateText.visibility = if (cbAltTextActivePassive.isChecked) View.VISIBLE else View.GONE
    }


  }

  private fun checkLogin() {
    loading.visibility = View.VISIBLE
    preferencesViewModel.loginCheck()
  }

  private fun onClickDownloadButton(spinner: Spinner, direction: Int) {
    if (membershipState != ConstantVariables.MEMBER_IS_FOUND) {
      openLoginActivity()
      return
    }
    val downloadableSelectedItem = spinner.selectedItem as EditionAdapterData
    preferencesViewModel.download(downloadableSelectedItem.value, direction)
  }

  private fun openLoginActivity() {
    FlowController.launchLoginActivity(this)
    finish()
  }

  private fun prepareScreenByMembership(value: Int) {
    loading.visibility = View.GONE
    this.membershipState = value

    btnDownloadTop.isEnabled = membershipState != ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION
    btnDownloadBottom.isEnabled = membershipState != ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION
    progressTop.visibility = if (membershipState != ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION) View.VISIBLE else View.GONE
    progressBottom.visibility = if (membershipState != ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION) View.VISIBLE else View.GONE
    btnDownloadTop.visibility = View.VISIBLE
    btnDownloadBottom.visibility = View.VISIBLE

    tvLoginMessage.isEnabled = false
    when (membershipState) {
      ConstantVariables.MEMBER_IS_FOUND -> lifecycleScope.launch {
        val memberInfo = preferencesViewModel.getMemberInfo()
        tvLoginMessage.text = getString(R.string.welcome_message_for_member, "${memberInfo[0].name} ${memberInfo[0].surname}")
        btnDownloadTop.text = getString(R.string.download)
        btnDownloadBottom.text = getString(R.string.download)
      }
      ConstantVariables.MEMBER_IS_NOT_FOUND -> {
        btnDownloadTop.text = getString(R.string.order)
        btnDownloadBottom.text = getString(R.string.order)
        tvLoginMessage.isEnabled = true
        tvLoginMessage.text = getString(R.string.signup_warning)
      }
      ConstantVariables.SESSION_IS_DIFFERENT -> {
        showErrorDialog(getString(R.string.logout_due_to_session_number_is_different))
        updateMaxAyahCount()
        btnDownloadTop.text = getString(R.string.order)
        btnDownloadBottom.text = getString(R.string.order)
        tvLoginMessage.isEnabled = true
        tvLoginMessage.text = getString(R.string.signup_warning)
      }
      else -> {
        tvLoginMessage.text = getString(R.string.local_mode_warning)
        tvProgressTextTop.text = ""
        tvProgressTextBottom.text = ""
        btnDownloadTop.text = getString(R.string.offline)
        btnDownloadBottom.text = getString(R.string.offline)
      }
    }
  }

  private fun percentageSurahTranslate(it: IntArray?,
                                       progress: ProgressBar,
                                       tvProgressText: TextView,
                                       btnDownload: Button) {
    btnDownload.visibility = View.INVISIBLE
    it?.let {
      progress.max = it[0]
      val isDone = (it[0]) == it[1]
      progress.progress = it[1]
      tvProgressText.text = if (isDone)
        getString(R.string.download_finished)
      else
        calculatePercentage(R.string.surah_translate_is_downloading, it[1], it[0])
      btnDownload.visibility = if (isDone) View.VISIBLE else View.INVISIBLE
      progress.visibility = if (isDone) View.GONE else View.VISIBLE
      tvProgressText.visibility = if (isDone) View.GONE else View.VISIBLE
      Log.e("BBB", "${(it[0])} -- ${it[1]}")
    }
  }

  private fun percentageDownload(it: Int?,
                                 progress: ProgressBar,
                                 tvProgressText: TextView,
                                 btnDownload: Button) {
    it?.let {
      progress.max = ConstantVariables.MAX_SURAH_NUMBER
      progress.progress = it
      tvProgressText.text = if (it == ConstantVariables.MAX_SURAH_NUMBER)
        getString(R.string.download_finished)
      else
        calculatePercentage(R.string.surah_ayahs_is_downloading, it, ConstantVariables.MAX_SURAH_NUMBER)
      val isDone = it == ConstantVariables.MAX_SURAH_NUMBER
      btnDownload.visibility = if (isDone) View.VISIBLE else View.INVISIBLE
      progress.visibility = if (isDone) View.GONE else View.VISIBLE
      tvProgressText.visibility = if (isDone) View.GONE else View.VISIBLE
    }
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

  private fun updateMaxAyahCount() {
    lifecycleScope.launch {
      var maxAyahCount = ConstantVariables.MAX_FREE_AYAH_NUMBER
      if (membershipState == ConstantVariables.MEMBER_IS_FOUND) {
        val result = preferencesViewModel.getMaxAyahCount()
        maxAyahCount = result.max
      }
      Log.e("AAA", "=====> MAX AYAH COUNT $maxAyahCount")
      cache.updateMaxAyahCount(maxAyahCount)
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

  override fun onNetworkConnectionChanged(isConnected: Boolean) {
    super.onNetworkConnectionChanged(isConnected)
    if (isConnected) {
      checkLogin()
    } else {
      prepareScreenByMembership(ConstantVariables.LOCAL_MODE_DUE_TO_CONNECTION)
    }
  }


}

