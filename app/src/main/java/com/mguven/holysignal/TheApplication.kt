package com.mguven.holysignal

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.mguven.holysignal.di.component.ApplicationComponent
import com.mguven.holysignal.di.component.DaggerApplicationComponent
import com.mguven.holysignal.di.module.ApplicationModule
import com.squareup.picasso.Picasso
import javax.inject.Inject


class TheApplication : Application() {

  lateinit var applicationComponent: ApplicationComponent

  @Inject
  lateinit var picasso: Picasso

  override fun onCreate() {
    super.onCreate()
    inject()
  }

  fun isOnline(): Boolean {
    val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return cm!!.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
  }

  private fun inject() {
    applicationComponent = DaggerApplicationComponent
        .builder()
        .applicationModule(ApplicationModule(this))
        .build()

    applicationComponent.inject(this)
  }
}
