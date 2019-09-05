package com.mguven.holysignal.di.component

import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.di.module.ActivityModule

import dagger.Subcomponent

@Subcomponent(modules = arrayOf(ActivityModule::class))
interface AbstractBaseComponent {

  fun inject(activity: AbstractBaseActivity)
}
