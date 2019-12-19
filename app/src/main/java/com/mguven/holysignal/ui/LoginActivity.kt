package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.module.LoginActivityModule
import com.mguven.holysignal.exception.Exceptions
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.loadingprogress.*
import javax.inject.Inject


class LoginActivity : AbstractBaseActivity() {

  @Inject
  lateinit var deviceUtil: DeviceUtil

  private lateinit var preferencesViewModel: PreferencesViewModel

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(LoginActivityModule(this))
        .inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    inject()
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    btnSignIn.setOnClickListener {
      if (deviceUtil.isEmailValid(tvEmail.editText?.text.toString())) {
        loading.visibility = View.VISIBLE
        preferencesViewModel.signIn(tvEmail.editText?.text.toString(),
            tvPassword.editText?.text.toString())
      } else {
        showErrorDialog(getString(R.string.sign_in_error))
      }
    }

    btnSignUp.setOnClickListener {
      FlowController.launchSignUpActivity(this)
    }

    preferencesViewModel.memberShipData.observe(this, Observer<SignInEntity> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        FlowController.launchMainActivity(this)
        finish()
      } else {
        when (response.status) {
          Exceptions.SESSION_IS_DIFFERENT -> showYesNoDialog(getString(R.string.session_no_is_different),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          Exceptions.STORED_PHONE_IS_CHANGED -> showYesNoDialog(getString(R.string.phone_changed_warning),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          Exceptions.STORED_PHONE_CHANGED_MORE_THAN_TWO_TIMES_AND_IS_UNUSABLE_NOW -> {
            showErrorDialog(getString(R.string.phone_invalid))
          }
          else -> showErrorDialog(getString(R.string.sign_in_error))
        }
      }
    })

  }


}

