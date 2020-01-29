package com.mguven.holysignal

import android.content.Context
import android.content.Intent
import com.mguven.holysignal.ui.*

class FlowController {

  companion object {

    fun launchMainActivity(context: Context, clearTask: Boolean = false) {
      val i = Intent(context, MainActivity::class.java)
      if (clearTask) {
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      context.startActivity(i)
    }

    fun launchLoginActivity(context: Context, clearTask: Boolean = false) {
      val i = Intent(context, LoginActivity::class.java)
      if (clearTask) {
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      context.startActivity(i)
    }

    fun launchSignUpActivity(context: Context) {
      context.startActivity(
          Intent(context, SignupActivity::class.java)
      )
    }

    fun launchForgotPasswordActivity(context: Context) {
      context.startActivity(
          Intent(context, ForgotPasswordActivity::class.java)
      )
    }

    fun launchCardActivity(context: Context, clearTask: Boolean = false) {
      val intent =
          Intent(context.applicationContext, CardActivity::class.java)
      if (clearTask) {
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }

    fun launchDownloadActivity(context: Context) {
      context.startActivity(
          Intent(context, DownloadActivity::class.java)
      )
    }

    fun launchSendAsAGift(context: Context) {
      context.startActivity(
          Intent(context, SendAsAGiftActivity::class.java)
      )
    }

  }

}