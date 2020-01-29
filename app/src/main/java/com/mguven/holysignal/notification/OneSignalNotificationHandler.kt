package com.mguven.holysignal.notification

import android.content.Intent
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.ui.MainActivity
import com.onesignal.OSNotificationAction.ActionType
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler
import timber.log.Timber

class OneSignalNotificationHandler(private val application: TheApplication) : NotificationOpenedHandler {
  override fun notificationOpened(result: OSNotificationOpenResult) {
    val actionType = result.action.type
    val data = result.notification.payload.additionalData
    val customKey: String
    if (data != null) {
      customKey = data.optString("customkey", null)
      Timber.i("OneSignalExample customkey set with value: %s", customKey)
    }
    if (actionType == ActionType.ActionTaken) Timber.i("Button pressed with id: %s", result.action.actionID)
    val intent = Intent(application, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
    application.startActivity(intent)
  }

}
