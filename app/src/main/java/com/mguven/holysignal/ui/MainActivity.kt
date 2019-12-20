package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.job.LockScreenJob
import com.mguven.holysignal.model.TimePreference
import com.mguven.holysignal.ui.adapter.SearchableSpinnerAdapter
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loadingprogress.*
import kotlinx.coroutines.launch


class MainActivity : AbstractBaseActivity() {

  private lateinit var spannerList: List<EditionAdapterData>
  private lateinit var preferencesViewModel: PreferencesViewModel
  var membershipState = ConstantVariables.MEMBER_IS_NOT_FOUND
  private val arrHours by lazy {
    val arr = arrayListOf<String>()
    for (x in 0..23) {
      arr.add(if (x < 10) "0$x" else x.toString())
    }
    return@lazy arr
  }

  private val arrMinutes by lazy {
    val arr = arrayListOf<String>()
    for (x in 0..59) {
      arr.add(if (x < 10) "0$x" else x.toString())
    }
    return@lazy arr
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    inject(MainActivityModule(this))
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)
    initTimePreference()

    cbActivePassive.isChecked = cache.isActive()

    cbTimePreference.isChecked = cache.isTimePreferenceCheckboxChecked()
    cbTimePreference.setOnClickListener {
      onTimePreferenceCheckboxChanged(cbTimePreference.isChecked)
    }

    arrangeTimePreferenceCheckboxVisibility()
    arrangeTimePreferences(cbTimePreference.isChecked)

    cbAltTextActivePassive.isChecked = cache.hasSecondLanguageSupport()
    clAlternateText.visibility = if (cbAltTextActivePassive.isChecked) View.VISIBLE else View.GONE

    cbTopOnlyFull.setOnClickListener {
      initEditionSpinners()
    }

    runJobScheduler()

    initEditionSpinners()

    btnOk.setOnClickListener {
      val topTextEditionSpinnerSelectedItem = spannerList[spTopTextEdition.selectedItemPosition]
      val bottomTextEditionSpinnerSelectedItem = spannerList[spBottomTextEdition.selectedItemPosition]
      cache.updateTopTextEditionId(topTextEditionSpinnerSelectedItem.value)
      cache.updateBottomTextEditionId(bottomTextEditionSpinnerSelectedItem.value)
      updateMaxAyahCount()

      val spHourStart = (spHourStart.selectedItem as String).toInt()
      val spMinuteStart = (spMinuteStart.selectedItem as String).toInt()
      val spHourFinish = (spHourFinish.selectedItem as String).toInt()
      val spMinuteFinish = (spMinuteFinish.selectedItem as String).toInt()

      if (((spHourFinish * 60) + spMinuteFinish) <= ((spHourStart * 60) + spMinuteStart)) {
        Toast.makeText(this, getString(R.string.finish_date_can_not_be_lower_than_start_date), Toast.LENGTH_SHORT).show()
      } else {
        val timePreference = TimePreference(spHourStart, spHourFinish, spMinuteStart, spMinuteFinish)
        cache.updateTimePreference(timePreference)
        Toast.makeText(this, getString(R.string.preferences_saved), Toast.LENGTH_SHORT).show()
        FlowController.launchCardActivity(this, true)
      }
    }

    preferencesViewModel.isMember.observe(this, Observer<Int> {
      prepareScreenByMembership(it)
    })

    tvLoginMessage.setOnClickListener {
      openLoginActivity()
    }

    cbActivePassive.setOnClickListener {
      cache.updateActivePassive(cbActivePassive.isChecked)
      arrangeTimePreferenceCheckboxVisibility()
    }

    cbAltTextActivePassive.setOnClickListener {
      cache.updateSecondLanguageSupport(cbAltTextActivePassive.isChecked)
      clAlternateText.visibility = if (cbAltTextActivePassive.isChecked) View.VISIBLE else View.GONE
    }

    btnDownload.setOnClickListener {
      FlowController.launchDownloadActivity(this)
    }

    btnSignOut.setOnClickListener {
      showYesNoDialog(getString(R.string.sign_out_warning_message), DialogInterface.OnClickListener { dialog, yes ->
        cache.clear()
        FlowController.launchMainActivity(this, true)
        dialog.dismiss()
      }, DialogInterface.OnClickListener { dialog, no ->
        dialog.dismiss()
      })
    }

    btnSignIn.setOnClickListener {
      FlowController.launchLoginActivity(this)
    }
  }

  private fun arrangeTimePreferenceCheckboxVisibility() {
    cbTimePreference.visibility = if (cbActivePassive.isChecked) View.VISIBLE else View.GONE
    val gone = (!cbActivePassive.isChecked) || (cbTimePreference.isChecked)
    spHourStart.visibility = if (gone) View.GONE else View.VISIBLE
    spMinuteStart.visibility = if (gone) View.GONE else View.VISIBLE
    spHourFinish.visibility = if (gone) View.GONE else View.VISIBLE
    spMinuteFinish.visibility = if (gone) View.GONE else View.VISIBLE
    tvColon.visibility = if (gone) View.GONE else View.VISIBLE
  }

  private fun initTimePreference() {
    val hourAdapter: ArrayAdapter<String> = timeAdapter(arrHours)
    spHourStart.adapter = hourAdapter
    spHourFinish.adapter = hourAdapter

    val minutesAdapter: ArrayAdapter<String> = timeAdapter(arrMinutes)
    spMinuteStart.adapter = minutesAdapter
    spMinuteFinish.adapter = minutesAdapter
  }

  private fun onTimePreferenceCheckboxChanged(isChecked: Boolean) {
    cache.updateTimePreferenceCheckboxState(isChecked)
    arrangeTimePreferences(isChecked)
  }

  private fun arrangeTimePreferences(isChecked: Boolean) {
    val gone = (!cbActivePassive.isChecked) || (isChecked)
    spHourStart.visibility = if (gone) View.GONE else View.VISIBLE
    spMinuteStart.visibility = if (gone) View.GONE else View.VISIBLE
    spHourFinish.visibility = if (gone) View.GONE else View.VISIBLE
    spMinuteFinish.visibility = if (gone) View.GONE else View.VISIBLE
    tvColon.visibility = if (gone) View.GONE else View.VISIBLE

    cbTimePreference.setText(if (isChecked) R.string.works_all_day else R.string.works_by_specific_time_period)
    if (isChecked) {
      spHourStart.setSelection(0)
      spMinuteStart.setSelection(0)
      spHourFinish.setSelection(spHourStart.adapter.count - 1)
      spMinuteFinish.setSelection(spMinuteFinish.adapter.count - 1)
    } else {
      val timePreference = cache.getTimePreference()
      spHourStart.setSelection(timePreference.hourStart)
      spMinuteStart.setSelection(timePreference.minuteStart)
      spHourFinish.setSelection(timePreference.hourFinish)
      spMinuteFinish.setSelection(timePreference.minuteFinish)
    }

  }

  private fun timeAdapter(arr: ArrayList<String>): ArrayAdapter<String> {
    val dataAdapter: ArrayAdapter<String> = ArrayAdapter(this,
        R.layout.support_simple_spinner_dropdown_item, arr)
    dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
    return dataAdapter
  }

  private fun checkLogin() {
    loading.visibility = View.VISIBLE
    preferencesViewModel.loginCheck()
  }

  private fun openLoginActivity() {
    FlowController.launchLoginActivity(this)
    finish()
  }

  private fun prepareScreenByMembership(value: Int) {
    loading.visibility = View.GONE
    btnSignOut.visibility = View.GONE
    btnSignIn.visibility = View.GONE
    this.membershipState = value

    tvLoginMessage.isEnabled = false
    when (membershipState) {
      ConstantVariables.MEMBER_IS_FOUND -> lifecycleScope.launch {
        val memberInfo = preferencesViewModel.getMemberInfo()
        tvLoginMessage.text = getString(R.string.welcome_message_for_member, "${memberInfo[0].name} ${memberInfo[0].surname}")
        btnDownload.visibility = View.VISIBLE
        btnSignOut.visibility = View.VISIBLE
      }
      ConstantVariables.MEMBER_IS_NOT_FOUND -> {
        tvLoginMessage.isEnabled = true
        tvLoginMessage.text = getString(R.string.signup_warning)
        tvLoginMessage.setTextColor(ContextCompat.getColor(this, R.color.error))
        btnSignIn.visibility = View.VISIBLE
      }
      ConstantVariables.SESSION_IS_DIFFERENT -> {
        showErrorDialog(getString(R.string.logout_due_to_session_number_is_different))
        cache.updateToken("token")
        updateMaxAyahCount()
        tvLoginMessage.isEnabled = true
        tvLoginMessage.setTextColor(ContextCompat.getColor(this, R.color.error))
        btnSignIn.visibility = View.VISIBLE
        tvLoginMessage.text = getString(R.string.signup_warning)
      }
      else -> {
        btnSignIn.visibility = View.VISIBLE
        tvLoginMessage.text = getString(R.string.local_mode_warning)
      }
    }
  }

  private fun initEditionSpinners(selectedItem: Int = 0) {
    spTopTextEdition.setTitle(getString(R.string.select_book))
    lifecycleScope.launch {
      spannerList = preferencesViewModel.getEditionNameIdList(cbTopOnlyFull.isChecked)
      btnOk.visibility = if (spannerList.isNotNullAndNotEmpty()) View.VISIBLE else View.GONE

      val adapter = SearchableSpinnerAdapter(this@MainActivity, R.layout.status_item, spannerList.map { it.key }, selectedItem)
      spTopTextEdition.adapter = adapter
      spannerList.forEachIndexed { index, it ->
        if (it.value == cache.getTopTextEditionId()) {
          spTopTextEdition.setSelection(index)
          return@forEachIndexed
        }
      }

      spBottomTextEdition.adapter = adapter
      spannerList.forEachIndexed { index, it ->
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

      //Cancel pending job scheduler if multiple instance are running.
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

