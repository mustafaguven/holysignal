package com.mguven.holysignal.di.module

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.mguven.holysignal.di.scope.PerActivity
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
open class FragmentModule(private val fragment: Fragment) {

  @PerActivity
  @Provides
  fun provideFragment(): Fragment = fragment

  @Provides
  fun providesLayoutInflater(): LayoutInflater = LayoutInflater.from(fragment.context)

  @Provides
  fun providesCompositeDisposable(): CompositeDisposable = CompositeDisposable()

}
