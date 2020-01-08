package com.mguven.holysignal.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.ui.fragment.SelectBookByTheLanguageFragment
import com.mguven.holysignal.ui.fragment.SelectLanguageFragment
import com.mguven.holysignal.viewmodel.DownloadViewModel

class ForgotPasswordActivity : AbstractBaseActivity(), SelectLanguageFragment.LanguageListener,
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
          //Toast.makeText(this@DownloadActivity, "Connected", Toast.LENGTH_SHORT).show()
        } else {
          Log.e("AAA", "NOT Connected")
          //Toast.makeText(this@DownloadActivity, "Can not connect", Toast.LENGTH_SHORT).show()
        }
      }
    })
  }

  override fun onLanguageSelected(languageData: LanguageData) {
    addFragment(SelectBookByTheLanguageFragment.newInstance(languageData))
  }

  override fun downloadRequested() {
    doPayment()
  }

  private fun doPayment() {
    if (BuildConfig.DEBUG) {
      startDownload()
      return
    }
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
      startDownload()
    }
  }

  private fun startDownload() {
    downloadViewModel.addDownload()
    (supportFragmentManager.findFragmentById(R.id.fragment) as SelectBookByTheLanguageFragment).downloadBook()
  }
}

