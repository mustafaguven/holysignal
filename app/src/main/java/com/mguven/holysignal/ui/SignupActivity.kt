package com.mguven.holysignal.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.di.module.SignupActivityModule
import com.mguven.holysignal.exception.Exceptions
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.loadingprogress.*
import javax.inject.Inject


class SignupActivity : AbstractBaseActivity() {

  @Inject
  lateinit var deviceUtil: DeviceUtil

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_signup)
    inject()
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    btnSignUp.setOnClickListener {
      val name = tvName.editText?.text.toString()
      val surname = tvSurname.editText?.text.toString()
      val email = tvEmail.editText?.text.toString()
      val password = tvPassword.editText?.text.toString()

      if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || !deviceUtil.isEmailValid(tvEmail.editText?.text.toString())) {
        showErrorDialog(getString(R.string.signup_empty_fields))
        return@setOnClickListener
      }


      loading.visibility = View.VISIBLE
      preferencesViewModel.signUp(name, surname, email, password)
    }

    preferencesViewModel.memberShipData.observe(this, Observer<SignInEntity> { response ->
      loading.visibility = View.GONE
      if (response.status == ConstantVariables.RESPONSE_OK) {
        FlowController.launchMainActivity(this, true)
        finish()
      } else {
        when {
          response.status == Exceptions.THIS_EMAIL_IS_ALREADY_IN_USE -> {
            Toast.makeText(this, R.string.signup_already_email, Toast.LENGTH_SHORT).show()
          }
          else -> showErrorDialog(response.message)
        }
      }
    })

  }

  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(SignupActivityModule(this))
        .inject(this)
  }


}

