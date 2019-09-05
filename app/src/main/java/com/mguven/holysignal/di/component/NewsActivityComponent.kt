package com.mguven.holysignal.di.component

import com.mguven.holysignal.ui.NewsActivity
import com.mguven.holysignal.di.module.NewsActivityModule

import dagger.Subcomponent

@Subcomponent(modules = [NewsActivityModule::class])
interface NewsActivityComponent {
  fun inject(newsActivity: NewsActivity)
}
