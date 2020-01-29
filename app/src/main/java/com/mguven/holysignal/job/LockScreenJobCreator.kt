package com.mguven.holysignal.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class LockScreenJobCreator : JobCreator {

  override fun create(tag: String): Job? {
    return when (tag) {
      LockScreenJob.TAG_I, LockScreenJob.TAG_P -> LockScreenJob()
      else -> null
    }
  }
}