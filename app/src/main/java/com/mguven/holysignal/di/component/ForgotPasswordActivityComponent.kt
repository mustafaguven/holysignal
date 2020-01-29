package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.ForgotPasswordActivityModule
import com.mguven.holysignal.ui.ForgotPasswordActivity
import dagger.Subcomponent

@Subcomponent(modules = [ForgotPasswordActivityModule::class])
interface ForgotPasswordActivityComponent {
  fun inject(forgotPasswordActivity: ForgotPasswordActivity)
}
