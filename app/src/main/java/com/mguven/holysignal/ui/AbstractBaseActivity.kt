package com.mguven.holysignal.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
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
import com.mguven.holysignal.ui.fragment.AbstractBaseFragment
import com.mguven.holysignal.util.ConnectivityReceiver
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


abstract class AbstractBaseActivity : AppCompatActivity(), LifecycleObserver, ConnectivityReceiver.ConnectivityReceiverListener {

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  @Inject
  lateinit var compositeDisposable: CompositeDisposable

  @Inject
  lateinit var cache: ApplicationCache

  private var component: AbstractBaseComponent? = null

  val receiver = ConnectivityReceiver()

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

  override fun onStart() {
    super.onStart()
    registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
  }

  override fun onStop() {
    super.onStop()
    unregisterReceiver(receiver)
    if (!compositeDisposable.isDisposed) {
      compositeDisposable.dispose()
    }
  }

  fun showSnackbar(str: String, isError: Boolean = false) {
    val snackbar = Snackbar.make(findViewById(android.R.id.content),
        str,
        Snackbar.LENGTH_SHORT)
    val textView = snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    snackbar.view.setBackgroundColor(ContextCompat.getColor(this, if (isError) R.color.error else R.color.black))
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
    snackbar.show()
  }

  fun showErrorSnackBar(res: Int) {
    showErrorSnackBar(getString(res))
  }

  fun showErrorSnackBar(errorMessage: String) {
    showSnackbar(errorMessage, true)
  }

  fun showYesNoDialog(question: String,
                      positiveCallback: DialogInterface.OnClickListener,
                      negativeCallback: DialogInterface.OnClickListener) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(question)
        .setTitle(R.string.warning)
        .setPositiveButton(getString(R.string.yes), positiveCallback)
        .setNegativeButton(getString(R.string.no), negativeCallback).show()
  }

  fun showErrorDialog(error: String) {
    val dialog = DialogInterface.OnClickListener { dialog, neutral ->
      dialog.dismiss()
    }
    val builder = AlertDialog.Builder(this)
    builder.setMessage(error)
        .setTitle(R.string.error)
        .setNeutralButton(getString(R.string.ok), dialog).show()
  }

  fun showInfoDialog(info: String, dialogListener: DialogInterface.OnClickListener? = null) {
    val dialog = DialogInterface.OnClickListener { dialog, neutral ->
      dialog.dismiss()
    }
    val builder = AlertDialog.Builder(this)
    builder.setMessage(info)
        .setTitle(R.string.info)
        .setNeutralButton(getString(R.string.ok), dialogListener ?: dialog).show()
  }

  override fun onNetworkConnectionChanged(isConnected: Boolean) {
    Log.e("AAA", "network changed")
  }

  override fun onResume() {
    super.onResume()
    ConnectivityReceiver.connectivityReceiverListener = this
  }


  protected fun addFragment(fragment: AbstractBaseFragment) {
    val ft = supportFragmentManager.beginTransaction()
    ft.replace(R.id.fragment, fragment, fragment::class.java.simpleName)
    ft.addToBackStack(null)
    ft.commit()
  }

}
