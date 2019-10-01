package com.mguven.holysignal

import android.app.Application
import com.evernote.android.job.JobManager
import com.mguven.holysignal.di.component.ApplicationComponent
import com.mguven.holysignal.di.component.DaggerApplicationComponent
import com.mguven.holysignal.di.module.ApplicationModule
import com.mguven.holysignal.job.LockScreenJobCreator
import com.squareup.picasso.Picasso
import javax.inject.Inject


class TheApplication : Application() {

  lateinit var applicationComponent: ApplicationComponent

  @Inject
  lateinit var picasso: Picasso

  override fun onCreate() {
    super.onCreate()
    JobManager.create(this).addJobCreator(LockScreenJobCreator())
    inject()
  }

  private fun inject() {
    applicationComponent = DaggerApplicationComponent
        .builder()
        .applicationModule(ApplicationModule(this))
        .build()

    applicationComponent.inject(this)
  }
}
