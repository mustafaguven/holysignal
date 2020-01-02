package com.mguven.holysignal.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.BundleKey
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.di.module.SignupActivityModule
import com.mguven.holysignal.exception.Exceptions
import com.mguven.holysignal.model.response.SignInEntity
import com.mguven.holysignal.sociallogin.AuthenticationService
import com.mguven.holysignal.sociallogin.AuthStateManager
import com.mguven.holysignal.sociallogin.Configuration
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.loadingprogress.*
import net.openid.appauth.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
        showInfoDialog(getString(R.string.email_is_waiting_for_verification),
            DialogInterface.OnClickListener { dialog, neutral ->
              FlowController.launchMainActivity(this, true)
              dialog.dismiss()
              finish()
            })
      } else {
        when (response.status) {
          Exceptions.THIS_EMAIL_IS_ALREADY_IN_USE -> {
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

