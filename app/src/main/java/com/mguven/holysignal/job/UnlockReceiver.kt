package com.mguven.holysignal.job

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.ui.CardActivity

class UnlockReceiver : BroadcastReceiver() {

  companion object {
    private val TAG = UnlockReceiver::class.java.simpleName
  }

  override fun onReceive(context: Context, intent: Intent) {

    if (BuildConfig.DEBUG) {
      Log.i(TAG, "onReceive: " + intent.action!!)
    }

    Log.e("AAA", "screen is off")
    if (intent.action!!.equals(Intent.ACTION_SCREEN_OFF, ignoreCase = true)) {
      //TODO:: perform action for SCREEN_OFF
      val intent =
          Intent(context.applicationContext, CardActivity::class.java)
      intent.addFlags(
          Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    } else if (intent.action!!.equals(Intent.ACTION_SCREEN_ON, ignoreCase = true)) {
      Log.e("AAA", "screen is on")
    }
  }

}