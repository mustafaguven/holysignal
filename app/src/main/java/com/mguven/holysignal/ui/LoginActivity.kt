package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.di.module.LoginActivityModule
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.loadingprogress.*


class LoginActivity : AbstractBaseActivity() {

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    inject(LoginActivityModule(this))
    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    btnSignIn.setOnClickListener {
      loading.visibility = View.VISIBLE
      preferencesViewModel.signIn(tvEmail.editText?.text.toString(),
          tvPassword.editText?.text.toString())
    }

    btnSignUp.setOnClickListener{
      FlowController.launchSignUpActivity(this)
    }

    preferencesViewModel.memberShipData.observe(this, Observer<SignInEntity> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        FlowController.launchMainActivity(this)
        finish()
      } else {
        when {
          response.status == 101 -> showYesNoDialog(getString(R.string.session_no_is_different),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          response.status == 102 -> showYesNoDialog(getString(R.string.phone_changed_warning),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          response.status == 103 -> {
            Toast.makeText(this, R.string.phone_invalid, Toast.LENGTH_SHORT).show()
          }
          else -> showErrorSnackBar(R.string.sign_in_error)
        }
      }
    })

  }


}

