package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.SignupActivityModule
import com.mguven.holysignal.ui.SignupActivity
import dagger.Subcomponent

@Subcomponent(modules = [SignupActivityModule::class])
interface SignupActivityComponent {
  fun inject(signupActivity: SignupActivity)
}
