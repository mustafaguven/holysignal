package com.mguven.holysignal.job

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.cache.ApplicationCache
import org.joda.time.DateTime

class UnlockReceiver : BroadcastReceiver() {

  companion object {
    private val TAG = UnlockReceiver::class.java.simpleName
  }

  override fun onReceive(context: Context, intent: Intent) {
    /*if (BuildConfig.DEBUG) {
      Log.i(TAG, "onReceive: " + intent.action!!)
    }*/

    val cache = (context.applicationContext as TheApplication).cache
    if (cache.isActive()) {
      if (intent.action!!.equals(Intent.ACTION_SCREEN_OFF, ignoreCase = true)) {
        Log.e("AAA", "screen is off")
        if (cache.isTimePreferenceCheckboxChecked() || canShow(cache)) {
          Log.e("AAA", "screen is redirected")
          FlowController.launchCardActivity(context, true)
        }
      } /*else if (intent.action!!.equals(Intent.ACTION_SCREEN_ON, ignoreCase = true)) {
        Log.e("AAA", "screen is on")
      }*/
    }
  }

  private fun canShow(cache: ApplicationCache): Boolean {
    val timePreference = cache.getTimePreference()
    val startDate = DateTime.now().withHourOfDay(timePreference.hourStart).withMinuteOfHour(timePreference.minuteStart)
    val finishDate = DateTime.now().withHourOfDay(timePreference.hourFinish).withMinuteOfHour(timePreference.minuteFinish)
    return startDate.isBeforeNow && finishDate.isAfterNow
  }

}