package com.mguven.holysignal.util

import android.content.Context
import android.content.res.Resources
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.DisplayMetrics
import com.mguven.holysignal.BuildConfig
import java.io.IOException


class DeviceUtil(val context: Context) {

  companion object {
    private val STATUS_BAR_HEIGHT = "status_bar_height"
    private val DIMEN = "dimen"
    private val ANDROID = "android"
  }

  var audioListener: AudioListener? = null

  @Throws(InterruptedException::class, IOException::class)
  fun isConnected(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
  }

  private var mediaPlayer: MediaPlayer = MediaPlayer()
  @Throws(java.lang.Exception::class)
  fun playAudio(url: String) {
    if (isConnected()) {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
      try {
        if (mediaPlayer.isPlaying) {
          mediaPlayer.stop()
          audioListener?.onAudioFinished()
        } else {
          audioListener?.onAudioWaiting()
          mediaPlayer.reset()
          mediaPlayer.setDataSource(url)
          mediaPlayer.prepareAsync()
          mediaPlayer.setOnPreparedListener {
            audioListener?.onAudioStarted()
            mediaPlayer.start()
          }
          mediaPlayer.setOnCompletionListener { audioListener?.onAudioFinished() }
        }
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  fun stopAudio() {
    if (mediaPlayer.isPlaying) {
      mediaPlayer.stop()
      audioListener?.onAudioFinished()
    }
  }

  interface AudioListener {
    fun onAudioWaiting()
    fun onAudioStarted()
    fun onAudioFinished()
  }

  fun isEmailValid(email: CharSequence) = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()



  val statusBarHeight: Int
    get() {
      var result = 0
      val resourceId = context.resources.getIdentifier(STATUS_BAR_HEIGHT, DIMEN, ANDROID)
      if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
      }
      return result
    }


  fun convertDpToPixel(dp: Float): Float {
    val resources = context.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
  }

  fun convertPixelsToDp(px: Float): Float {
    val resources = context.resources
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
  }

  fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
  }

  fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
  }

  val osVersion = Build.VERSION.RELEASE

  val apiVersion = BuildConfig.VERSION_NAME

  val apiKey = BuildConfig.API_KEY

  val versionCode: Int = BuildConfig.VERSION_CODE

  val phoneBrand: String = Build.MANUFACTURER

  val phoneModel: String = Build.MODEL

  val versionName: String = BuildConfig.VERSION_NAME
}