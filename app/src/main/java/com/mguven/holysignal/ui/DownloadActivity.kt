package com.mguven.holysignal.ui

import android.os.Bundle
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.ui.fragment.SelectBookByTheLanguageFragment
import com.mguven.holysignal.ui.fragment.SelectLanguageFragment
import com.mguven.holysignal.viewmodel.DownloadViewModel

class DownloadActivity : AbstractBaseActivity(), SelectLanguageFragment.LanguageListener {

  private lateinit var downloadViewModel: DownloadViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_download)
    inject(MainActivityModule(this))
    downloadViewModel = getViewModel(DownloadViewModel::class.java)
    supportFragmentManager.beginTransaction().add(R.id.fragment, SelectLanguageFragment()).commit()
  }

  override fun onLanguageSelected(languageData: LanguageData) {
    addFragment(SelectBookByTheLanguageFragment.newInstance(languageData))
  }



}

