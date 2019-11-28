package com.mguven.holysignal.util

import android.content.Context
import java.io.IOException

class DeviceUtil(val context: Context) {

  @Throws(InterruptedException::class, IOException::class)
  fun isConnected(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
  }

}