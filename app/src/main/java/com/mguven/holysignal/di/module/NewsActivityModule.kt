package com.mguven.holysignal.di.module

import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.adapter.NewsAdapter
import dagger.Module
import dagger.Provides

@Module
class NewsActivityModule(activity: AbstractBaseActivity) : ActivityModule(activity) {

  @Provides
  fun provideAdapter(): NewsAdapter {
    return NewsAdapter()
  }
}
