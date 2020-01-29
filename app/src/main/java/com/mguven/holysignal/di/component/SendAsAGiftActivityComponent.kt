package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.SendAsAGiftActivityModule
import com.mguven.holysignal.ui.SendAsAGiftActivity
import dagger.Subcomponent

@Subcomponent(modules = [SendAsAGiftActivityModule::class])
interface SendAsAGiftActivityComponent {
  fun inject(sendAsAGiftActivity: SendAsAGiftActivity)
}
