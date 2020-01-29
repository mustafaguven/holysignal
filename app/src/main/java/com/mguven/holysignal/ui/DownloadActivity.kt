package com.mguven.holysignal.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.android.billingclient.api.*
import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.model.response.ResponseEntity
import com.mguven.holysignal.ui.fragment.SelectBookByTheLanguageFragment
import com.mguven.holysignal.ui.fragment.SelectLanguageFragment
import com.mguven.holysignal.viewmodel.DownloadViewModel
import kotlinx.android.synthetic.main.loadingprogress.*

class DownloadActivity : AbstractBaseActivity(), SelectLanguageFragment.LanguageListener,
    PurchasesUpdatedListener, SelectBookByTheLanguageFragment.BookListener {

  private lateinit var billingClient: BillingClient
  private lateinit var downloadViewModel: DownloadViewModel
  private val skuList = listOf("holysignal_premium")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_download)
    inject(MainActivityModule(this))
    downloadViewModel = getViewModel(DownloadViewModel::class.java)
    supportFragmentManager.beginTransaction().add(R.id.fragment, SelectLanguageFragment()).commit()
    setupBillingClient()

    downloadViewModel.paidUserLiveData.observe(this, Observer<ResponseEntity<Int>> { response ->
      loading.visibility = View.GONE
      if (response.data == 1) {
        startDownload()
      } else {
        beforeDoPayment()
      }
    })

    downloadViewModel.updateUserAsPaidLiveData.observe(this, Observer<ResponseEntity<String>> { response ->
      loading.visibility = View.GONE
      startDownload()
    })
  }

  private fun setupBillingClient() {
    billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingServiceDisconnected() {
        Log.e("AAA", "onBillingServiceDisconnected")
      }

      override fun onBillingSetupFinished(response: BillingResult?) {
        if (response?.responseCode == BillingClient.BillingResponseCode.OK) {
          Log.e("AAA", "Connected")
        } else {
          Log.e("AAA", "NOT Connected")
        }
      }
    })
  }

  override fun onLanguageSelected(languageData: LanguageData) {
    addFragment(SelectBookByTheLanguageFragment.newInstance(languageData))
  }

  override fun downloadRequested() {
    loading.visibility = View.VISIBLE
    downloadViewModel.checkIsPaidUser()
  }

  private fun beforeDoPayment() {
    if (BuildConfig.DEBUG) {
      showYesNoDialog("doPayment?", DialogInterface.OnClickListener { dialogInterface, i ->
        doPayment()
        dialogInterface.dismiss()
      }, DialogInterface.OnClickListener { dialogInterface, i ->
        startDownload()
        dialogInterface.dismiss()
      })
      return
    }

    doPayment()
  }

  private fun doPayment() {
    if (billingClient.isReady) {
      val params = SkuDetailsParams.newBuilder()
          .setSkusList(skuList)
          .setType(BillingClient.SkuType.INAPP)
          .build()

      billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailList ->
        if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
          val flowParams = BillingFlowParams.newBuilder()
              .setSkuDetails(skuDetailList[0])
              .build()
          billingClient.launchBillingFlow(this, flowParams)
        } else {
          showErrorSnackBar(R.string.sku_detail_list_not_found)
        }
      }
    } else {
      showErrorSnackBar(R.string.billing_client_is_not_ready)
    }
  }

  override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
    if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {
        acknowledgePurchase(purchase.purchaseToken)
      }
    } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
      Toast.makeText(this, getString(R.string.user_canceled), Toast.LENGTH_SHORT).show()
    } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
      startDownload()
    } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
      Toast.makeText(this, getString(R.string.item_not_owned), Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(this, getString(R.string.error_occured_while_purchasing), Toast.LENGTH_SHORT).show()
    }
  }

  private fun acknowledgePurchase(purchaseToken: String) {
    val params = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchaseToken)
        .build()
    billingClient.acknowledgePurchase(params) { billingResult ->
      val responseCode = billingResult.responseCode
      val debugMessage = billingResult.debugMessage
      loading.visibility = View.VISIBLE
      downloadViewModel.setMemberAsPaid()
    }
  }

  private fun startDownload() {
    (supportFragmentManager.findFragmentById(R.id.fragment) as SelectBookByTheLanguageFragment).downloadBook()
  }
}

