package com.mguven.holysignal.di.module

import android.content.Context
import android.view.LayoutInflater
import com.mguven.holysignal.di.scope.PerActivity
import com.mguven.holysignal.ui.AbstractBaseActivity
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
open class ActivityModule(private val activity: AbstractBaseActivity) {

  @PerActivity
  @Provides
  fun provideContext(): Context = activity

  @Provides
  fun providesLayoutInflater(): LayoutInflater = LayoutInflater.from(activity)

  @Provides
  fun providesCompositeDisposable(): CompositeDisposable = CompositeDisposable()

}
