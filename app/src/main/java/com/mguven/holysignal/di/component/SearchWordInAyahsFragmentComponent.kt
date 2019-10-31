package com.mguven.holysignal.di.component

import com.mguven.holysignal.di.module.SearchWordInAyahsFragmentModule
import com.mguven.holysignal.ui.fragment.SearchWordInAyahsFragment
import dagger.Subcomponent

@Subcomponent(modules = [SearchWordInAyahsFragmentModule::class])
interface SearchWordInAyahsFragmentComponent {
  fun inject(searchWordInAyahsFragment: SearchWordInAyahsFragment)
}
