package com.mguven.holysignal.job

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.TheApplication

class UnlockReceiver : BroadcastReceiver() {

  companion object {
    private val TAG = UnlockReceiver::class.java.simpleName
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (BuildConfig.DEBUG) {
      Log.i(TAG, "onReceive: " + intent.action!!)
    }

    Log.e("AAA unlockreceiver", (context.applicationContext as TheApplication).cache.isActive().toString())
    if ((context.applicationContext as TheApplication).cache.isActive()) {
      Log.e("AAA", "screen is off")
      if (intent.action!!.equals(Intent.ACTION_SCREEN_OFF, ignoreCase = true)) {
        FlowController.launchCardActivity(context, true)
      } else if (intent.action!!.equals(Intent.ACTION_SCREEN_ON, ignoreCase = true)) {
        Log.e("AAA", "screen is on")
      }
    }
  }

}