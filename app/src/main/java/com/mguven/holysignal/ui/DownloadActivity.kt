package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.EditionAdapterData
import com.mguven.holysignal.db.entity.MaxAyahCountData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.job.LockScreenJob
import com.mguven.holysignal.ui.adapter.EditionAdapter
import com.mguven.holysignal.viewmodel.DownloadViewModel
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch


class DownloadActivity : AbstractBaseActivity() {

  private lateinit var downloadViewModel: DownloadViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    inject(MainActivityModule(this))
    downloadViewModel = getViewModel(DownloadViewModel::class.java)

  }


}

