package com.mguven.holysignal.di.module

import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.CardActivity
import com.mguven.holysignal.ui.adapter.AyahViewPagerAdapter
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import dagger.Module
import dagger.Provides

@Module
class CardActivityModule(val activity: AbstractBaseActivity) : ActivityModule(activity) {



  @Provides
  fun provideAyahViewPagerAdapter(activity: AbstractBaseActivity, holyBookViewModel: HolyBookViewModel, cache: ApplicationCache): AyahViewPagerAdapter {
    return AyahViewPagerAdapter(activity, activity.lifecycleScope , holyBookViewModel, cache)
  }

}
