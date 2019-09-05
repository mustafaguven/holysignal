package com.mguven.holysignal.di.component

import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.ViewModelModule
import com.mguven.holysignal.di.module.*

import javax.inject.Singleton

import dagger.Component

@Singleton
@Component(modules = [
  ApplicationModule::class,
  NetworkModule::class,
  ViewModelModule::class,
  SchedulerModule::class
])
interface ApplicationComponent {

  fun inject(theApplication: TheApplication)

  operator fun plus(activityModule: ActivityModule): AbstractBaseComponent

  operator fun plus(mainActivityModule: NewsActivityModule): NewsActivityComponent
}
