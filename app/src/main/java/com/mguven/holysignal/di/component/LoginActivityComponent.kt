package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.LoginActivityModule
import com.mguven.holysignal.ui.LoginActivity
import dagger.Subcomponent

@Subcomponent(modules = [LoginActivityModule::class])
interface LoginActivityComponent {
  fun inject(loginActivity: LoginActivity)
}