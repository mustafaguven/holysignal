package com.mguven.holysignal.di.module

import android.content.Context
import com.mguven.holysignal.db.ApplicationDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class DatabaseModule {

  @Provides
  @Singleton
  fun provideDatabase(applicationContext: Context): ApplicationDatabase = ApplicationDatabase(applicationContext)


}
