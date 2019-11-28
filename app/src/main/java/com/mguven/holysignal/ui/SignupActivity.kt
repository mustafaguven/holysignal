package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.di.module.SignupActivityModule
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.btnSignUp
import kotlinx.android.synthetic.main.activity_signup.tvEmail
import kotlinx.android.synthetic.main.activity_signup.tvPassword
import kotlinx.android.synthetic.main.loadingprogress.*


class SignupActivity : AbstractBaseActivity() {

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_signup)
    inject(SignupActivityModule(this))
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    btnSignUp.setOnClickListener{
      val name = tvName.editText?.text.toString()
      val surname = tvSurname.editText?.text.toString()
      val email = tvEmail.editText?.text.toString()
      val password = tvPassword.editText?.text.toString()

      if(name.isNullOrEmpty() || surname.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()){
        showErrorSnackBar(R.string.signup_empty_fields)
        return@setOnClickListener
      }

      loading.visibility = View.VISIBLE
      preferencesViewModel.signUp(name, surname, email, password)
    }

    preferencesViewModel.memberShipData.observe(this, Observer<SignInEntity> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        FlowController.launchMainActivity(this)
        finish()
      } else {
        when {
          response.status == 105 -> {
            Toast.makeText(this, R.string.signup_already_email, Toast.LENGTH_SHORT).show()
          }
          else -> showErrorSnackBar(response.message)
        }
      }
    })

  }


}

