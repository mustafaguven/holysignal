package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.SelectSurahFragmentModule
import com.mguven.holysignal.ui.fragment.SelectSurahFragment
import dagger.Subcomponent

@Subcomponent(modules = [SelectSurahFragmentModule::class])
interface SelectSurahFragmentComponent {
  fun inject(selectSurahFragment: SelectSurahFragment)
}
