package com.mguven.holysignal.di.module

import android.content.Context
import com.mguven.holysignal.util.DeviceUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class DeviceModule {

  @Provides
  @Singleton
  fun provideDeviceUtil(context: Context): DeviceUtil = DeviceUtil(context)



}
