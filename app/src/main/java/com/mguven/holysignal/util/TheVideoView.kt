package com.mguven.holysignal.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import com.mguven.holysignal.TheApplication
import timber.log.Timber


class TheVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    VideoView(context, attrs,
        defStyleAttr), OnPreparedListener, OnCompletionListener, OnErrorListener {

  companion object {
    private const val MIN: Int = 0
    private const val MAX: Int = 100
  }

  private val deviceUtil: DeviceUtil = DeviceUtil(context.applicationContext as TheApplication)
  private var mediaPlayer: MediaPlayer? = null
  private var videoController: MediaController? = null
  private var boundedViews: Array<View>? = null
  var isMuted: Boolean = true

  init {
    this.setOnPreparedListener(this)
    this.setOnCompletionListener(this)
    this.setOnErrorListener(this)
  }

  override fun onPrepared(mp: MediaPlayer?) {
    mediaPlayer = mp
    videoController?.visibility = View.GONE
    mediaPlayer?.isLooping = true
    mute()
    start()
    boundedViews?.forEach({ it.visibility = View.VISIBLE })
  }

  override fun onCompletion(mp: MediaPlayer?) {
    //do nothing
  }

  override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
    Timber.e(Exception("Splash video gosterilirken hata: $what"))
    boundedViews?.forEach({ it.visibility = View.GONE })
    return false
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    holder.setFixedSize(deviceUtil.getScreenHeight() * 2, deviceUtil.getScreenWidth() * 2)
    forceLayout()
    setMeasuredDimension(deviceUtil.getScreenHeight() * 2,
        deviceUtil.getScreenWidth() * 2)
    invalidate()
  }

  fun play(uri: Uri, boundedViews: Array<View>? = null) {
    visibility = View.VISIBLE
    videoController = MediaController(context)
    videoController?.setAnchorView(this)
    videoController?.setMediaPlayer(this)
    this.boundedViews = boundedViews
    setMediaController(videoController)
    setVideoURI(uri)
  }

  fun mute() {
    setVolume(MIN)
  }

  fun unMute() {
    setVolume(MAX)
  }

  private fun setVolume(amount: Int) {
    isMuted = (amount == MIN)
    val numerator: Double = if (MAX - amount > 0) {
      Math.log((MAX - amount).toDouble())
    } else {
      0.0
    }
    val volume = (1 - numerator / Math.log(MAX.toDouble())).toFloat()
    this.mediaPlayer?.setVolume(volume, volume)
  }

}