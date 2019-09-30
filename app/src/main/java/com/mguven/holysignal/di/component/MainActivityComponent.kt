package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.ui.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {
  fun inject(mainActivity: MainActivity)
}
