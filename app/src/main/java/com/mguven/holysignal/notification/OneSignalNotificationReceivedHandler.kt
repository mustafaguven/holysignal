package com.mguven.holysignal.notification

import com.mguven.holysignal.TheApplication
import com.onesignal.OSNotification
import com.onesignal.OneSignal.NotificationReceivedHandler
import timber.log.Timber

class OneSignalNotificationReceivedHandler(theApplication: TheApplication) : NotificationReceivedHandler {
  override fun notificationReceived(notification: OSNotification) {
    Timber.d("updatePricesIncrementally - 10")
  }

}
