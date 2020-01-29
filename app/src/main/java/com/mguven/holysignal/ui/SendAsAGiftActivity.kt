package com.mguven.holysignal.ui

import android.os.Bundle
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.module.SendAsAGiftActivityModule
import com.mguven.holysignal.di.module.SignupActivityModule
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import javax.inject.Inject


class SendAsAGiftActivity : AbstractBaseActivity() {

  @Inject
  lateinit var deviceUtil: DeviceUtil

  private lateinit var preferencesViewModel: PreferencesViewModel


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send_as_a_gift)
    inject()
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)
  }

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(SendAsAGiftActivityModule(this))
        .inject(this)
  }



}

