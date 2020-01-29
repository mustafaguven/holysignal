package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.module.ForgotPasswordActivityModule
import com.mguven.holysignal.model.SetNewPasswordResponseEntity
import com.mguven.holysignal.model.response.ResponseEntity
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.loadingprogress.*
import javax.inject.Inject

class ForgotPasswordActivity : AbstractBaseActivity() {

  @Inject
  lateinit var deviceUtil: DeviceUtil
  private lateinit var preferencesViewModel: PreferencesViewModel
  var validationCode = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forgot_password)
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)
    inject()
    btnOk.setOnClickListener {
      if (tvValidationCode.visibility == View.VISIBLE) {
        initNewPassword()
      } else {
        initPasswordRecovery()
      }
    }

    preferencesViewModel.sendPasswordLiveData.observe(this, Observer<ResponseEntity<Int>> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        validationCode = response.data.toString()
        tvTitle.text = getString(R.string.password_reset_step_2_tip)
        tvEmail.isEnabled = false
        tvValidationCode.visibility = View.VISIBLE
        tvNewPassword.visibility = View.VISIBLE
        tvNewPasswordConfirm.visibility = View.VISIBLE
      } else {
        showErrorDialog(getString(R.string.password_reset_error))
      }
    })

    preferencesViewModel.setPasswordLiveData.observe(this, Observer<ResponseEntity<SetNewPasswordResponseEntity>> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        showInfoDialog(getString(R.string.new_password_has_been_set_successfully), DialogInterface.OnClickListener { dialog, neutral ->
          FlowController.launchLoginActivity(this, true)
          dialog.dismiss()
          finish()
        })
      } else {
        showErrorDialog(response.message)
      }
    })
  }

  private fun initNewPassword() {
    val email = tvEmail.editText?.text.toString()
    val typedValidationCode = tvValidationCode.editText?.text.toString()
    val newPassword = tvNewPassword.editText?.text.toString()
    val newPasswordConfirm = tvNewPasswordConfirm.editText?.text.toString()
    if (typedValidationCode.isEmpty() || newPassword.isEmpty() || newPasswordConfirm.isEmpty() || newPassword != newPasswordConfirm) {
      showErrorDialog(getString(R.string.signup_empty_fields))
    } else if (typedValidationCode != validationCode) {
      showErrorDialog(getString(R.string.invalid_validation_code))
    } else {
      preferencesViewModel.setNewPassword(email, newPassword)
    }
  }

  private fun initPasswordRecovery() {
    val email = tvEmail.editText?.text.toString()
    if (email.isEmpty() || !deviceUtil.isEmailValid(email)) {
      showErrorDialog(getString(R.string.signup_empty_fields))
    } else {
      loading.visibility = View.VISIBLE
      preferencesViewModel.sendPasswordRecoveryMail(email)
    }
  }

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(ForgotPasswordActivityModule(this))
        .inject(this)
  }

}

