package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.CardActivityModule
import com.mguven.holysignal.ui.CardActivity
import dagger.Subcomponent

@Subcomponent(modules = [CardActivityModule::class])
interface CardActivityComponent {
  fun inject(cardActivity: CardActivity)
}
