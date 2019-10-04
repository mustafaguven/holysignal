package com.mguven.holysignal.job

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.mguven.holysignal.BuildConfig
import java.util.concurrent.TimeUnit

class LockScreenJob : Job() {

  override fun onRunJob(params: Job.Params): Job.Result {
    // run your job here

    val jobTag = params.tag

    if (BuildConfig.DEBUG) {
      Log.i(TAG, "Job started! $jobTag")
    }

    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    var isInteractive = false
    // Here we check current status of device screen, If it's Interactive then device screen is ON.
    if (Build.VERSION.SDK_INT >= 20) {
      isInteractive = pm.isInteractive
    } else {
      isInteractive = pm.isScreenOn
    }

    try {
      if (aks_Receiver != null) {
        context.applicationContext.unregisterReceiver(aks_Receiver) //Use 'Application Context'.
      }
    } catch (e: Exception) {
      if (BuildConfig.DEBUG) {
        e.printStackTrace()
      }
    } finally {
      aks_Receiver = null
    }

    try {
      //Register receiver for listen "SCREEN_OFF" and "SCREEN_ON" action.

      val filter = IntentFilter("android.intent.action.SCREEN_OFF")
      filter.addAction("android.intent.action.SCREEN_ON")
      aks_Receiver = UnlockReceiver()
      context.applicationContext.registerReceiver(aks_Receiver, filter) //use 'Application context' for listen brodcast in background while app is not running, otherwise it may throw an exception.
    } catch (e: Exception) {
      if (BuildConfig.DEBUG) {
        e.printStackTrace()
      }
    }

    if (isInteractive) {
      //TODO:: Can perform required action based on current status of screen.
    }

    return Job.Result.SUCCESS
  }

  companion object {

    private val TAG = LockScreenJob::class.java.simpleName

    val TAG_P = "periodic_job_tag"
    val TAG_I = "immediate_job_tag"

    //Used static refrence of broadcast receiver for ensuring if it's already register or not NULL
    // then first unregister it and set to null before registering it again.
    var aks_Receiver: UnlockReceiver? = null

    /**
     * scheduleJobPeriodic: Added a periodic Job scheduler which run on every 15 minute and register receiver if it's unregister. So by this hack broadcast receiver registered for almost every time w.o. running any foreground/ background service.
     * @return
     */
    fun scheduleJobPeriodic(): Int {

      return JobRequest.Builder(TAG_P)
          .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
          .setRequiredNetworkType(JobRequest.NetworkType.ANY)
          .build()
          .schedule()
    }

    /**
     * runJobImmediately: run job scheduler immediately so that broadcasr receiver also register immediately
     * @return
     */
    fun runJobImmediately(): Int {

      return JobRequest.Builder(TAG_I)
          .startNow()
          .build()
          .schedule()
    }

    /**
     * cancelJob: used for cancel any running job by their jobId.
     * @param jobId
     */
    fun cancelJob(jobId: Int) {
      JobManager.instance().cancel(jobId)
    }
  }
}