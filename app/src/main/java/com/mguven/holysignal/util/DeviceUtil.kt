package com.mguven.holysignal.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.IOException


class DeviceUtil(val context: Context) {

  var audioListener: AudioListener? = null

  @Throws(InterruptedException::class, IOException::class)
  fun isConnected(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
  }

  private var mediaPlayer: MediaPlayer = MediaPlayer()
  @Throws(java.lang.Exception::class)
  fun playAudio(url: String) {
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

  fun stopAudio() {
    if (mediaPlayer.isPlaying) {
      mediaPlayer.stop()
      audioListener?.onAudioFinished()
    }
  }

  fun isAudioPlaying() = mediaPlayer.isPlaying

  interface AudioListener {
    fun onAudioWaiting()
    fun onAudioStarted()
    fun onAudioFinished()
  }

}