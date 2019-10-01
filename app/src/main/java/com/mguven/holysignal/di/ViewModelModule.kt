package com.mguven.holysignal.di

import androidx.lifecycle.ViewModel
import com.mguven.holysignal.viewmodel.HolyBookViewModel

import com.mguven.holysignal.viewmodel.NewsViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

  @Binds
  @IntoMap
  @ViewModelKey(NewsViewModel::class)
  abstract fun bindListViewModel(viewModel: NewsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(HolyBookViewModel::class)
  abstract fun bindMainViewModel(viewModel: HolyBookViewModel): ViewModel

}
