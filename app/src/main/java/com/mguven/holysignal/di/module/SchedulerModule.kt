package com.mguven.holysignal.di.module

import com.mguven.holysignal.rx.AppSchedulerProvider
import com.mguven.holysignal.rx.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SchedulerModule {

  @Provides
  @Singleton
  fun provideSchedulerProvider(): SchedulerProvider = AppSchedulerProvider()

}