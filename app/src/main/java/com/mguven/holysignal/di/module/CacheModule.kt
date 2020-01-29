package com.mguven.holysignal.di.module

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mguven.holysignal.Constant
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.cache.ApplicationCache
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class CacheModule {

  @Provides
  @Singleton
  @Named(Constant.APPLICATION_SHARED_PREFERENCES)
  internal fun provideApplicationSharedPreference(
      application: TheApplication): SharedPreferences {
    return application.getSharedPreferences(Constant.APPLICATION_SHARED_PREFERENCES,
        Context.MODE_PRIVATE)
  }

  @Provides
  @Singleton
  internal fun provideApplicationCache(
      @Named(Constant.APPLICATION_SHARED_PREFERENCES) applicationPreferences: SharedPreferences,
      gson: Gson): ApplicationCache = ApplicationCache(
      applicationPreferences,
      gson)
}
