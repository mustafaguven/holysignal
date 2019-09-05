package com.mguven.holysignal.di.module

import android.app.ActivityManager
import android.content.Context
import com.mguven.holysignal.TheApplication
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val applicationContext: Context) {

  @Singleton
  @Provides
  fun provideApplicationContext(): Context {
    return applicationContext
  }

  @Singleton
  @Provides
  fun provideApplication(): TheApplication = applicationContext as TheApplication

  @Singleton
  @Provides
  fun providesPicasso(): Picasso {
    val builder = Picasso.Builder(applicationContext)
    builder.memoryCache(LruCache(getBytesForMemCache(12)))
    val requestTransformer = Picasso.RequestTransformer { request ->
      request
    }
    builder.requestTransformer(requestTransformer)
    val picasso = builder.build()
    Picasso.setSingletonInstance(picasso)
    return picasso
  }

  private fun getBytesForMemCache(percent: Int): Int {
    val mi = ActivityManager.MemoryInfo()
    val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(mi)
    val availableMemory = mi.availMem.toDouble()
    return (percent * availableMemory / 100).toInt()
  }

}
