package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.ActivityModule
import com.mguven.holysignal.ui.AbstractBaseActivity
import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class])
interface AbstractBaseComponent {

  fun inject(activity: AbstractBaseActivity)
}
