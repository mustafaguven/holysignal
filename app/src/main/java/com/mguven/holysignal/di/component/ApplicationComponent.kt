package com.mguven.holysignal.di.component

import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.ViewModelModule
import com.mguven.holysignal.di.module.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
  ApplicationModule::class,
  NetworkModule::class,
  DeviceModule::class,
  ViewModelModule::class,
  DatabaseModule::class,
  SchedulerModule::class,
  CacheModule::class,
  GsonModule::class
])
interface ApplicationComponent {

  fun inject(theApplication: TheApplication)

  operator fun plus(activityModule: ActivityModule): AbstractBaseComponent

  operator fun plus(newsActivityModule: NewsActivityModule): NewsActivityComponent

  operator fun plus(mainActivityModule: MainActivityModule): MainActivityComponent

  operator fun plus(cardActivityModule: CardActivityModule): CardActivityComponent

  operator fun plus(searchWordInAyahsFragmentModule: SearchWordInAyahsFragmentModule): SearchWordInAyahsFragmentComponent

  operator fun plus(selectSurahFragmentModule: SelectSurahFragmentModule): SelectSurahFragmentComponent

  operator fun plus(signupActivityModule: SignupActivityModule): SignupActivityComponent

  operator fun plus(loginActivityModule: LoginActivityModule): LoginActivityComponent
}
