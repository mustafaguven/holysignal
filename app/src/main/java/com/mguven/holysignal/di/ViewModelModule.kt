package com.mguven.holysignal.di

import androidx.lifecycle.ViewModel
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.viewmodel.MainViewModel

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
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

}
