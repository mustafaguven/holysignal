package com.mguven.holysignal.di

import androidx.lifecycle.ViewModel
import com.mguven.holysignal.viewmodel.DownloadViewModel
import com.mguven.holysignal.viewmodel.HolyBookViewModel

import com.mguven.holysignal.viewmodel.NewsViewModel
import com.mguven.holysignal.viewmodel.PreferencesViewModel

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
  abstract fun bindHolyBookViewModel(viewModel: HolyBookViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PreferencesViewModel::class)
  abstract fun bindPreferencesViewModel(viewModel: PreferencesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(DownloadViewModel::class)
  abstract fun bindDownloadViewModel(viewModel: DownloadViewModel): ViewModel

}
