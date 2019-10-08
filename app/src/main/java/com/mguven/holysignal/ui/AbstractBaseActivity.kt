package com.mguven.holysignal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.di.ViewModelFactory
import com.mguven.holysignal.di.component.AbstractBaseComponent
import com.mguven.holysignal.di.module.ActivityModule
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class AbstractBaseActivity : AppCompatActivity(), LifecycleObserver {

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  @Inject
  lateinit var compositeDisposable: CompositeDisposable

  @Inject
  lateinit var cache: ApplicationCache

  private var component: AbstractBaseComponent? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    component = (application as TheApplication)
        .applicationComponent
        .plus(ActivityModule(this))

    component!!.inject(this)
  }

  protected fun inject(module: ActivityModule) {
    (application as TheApplication)
        .applicationComponent
        .plus(module)
        .inject(this)
  }

  fun <T : ViewModel> getViewModel(viewModelClz: Class<T>): T =
      ViewModelProviders.of(this, viewModelFactory).get(viewModelClz)

  override fun onStop() {
    super.onStop()
    if (!compositeDisposable.isDisposed) {
      compositeDisposable.dispose()
    }
  }

  fun showSnackbar(str: String){
    val snackbar = Snackbar.make(findViewById(android.R.id.content),
        str,
        Snackbar.LENGTH_SHORT)
    snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
    snackbar.show()
  }
}
