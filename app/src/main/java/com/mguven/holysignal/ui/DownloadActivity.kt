package com.mguven.holysignal.ui

import android.os.Bundle
import android.widget.Toast
import com.android.billingclient.api.*
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.di.module.MainActivityModule
import com.mguven.holysignal.ui.fragment.SelectBookByTheLanguageFragment
import com.mguven.holysignal.ui.fragment.SelectLanguageFragment
import com.mguven.holysignal.viewmodel.DownloadViewModel

class DownloadActivity : AbstractBaseActivity(), SelectLanguageFragment.LanguageListener,
    PurchasesUpdatedListener, SelectBookByTheLanguageFragment.BookListener {

  private lateinit var billingClient: BillingClient
  private lateinit var downloadViewModel: DownloadViewModel

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
        Toast.makeText(this@DownloadActivity, "onBillingServiceDisconnected", Toast.LENGTH_SHORT).show()
      }

      override fun onBillingSetupFinished(response: BillingResult?) {
        if (response?.responseCode == BillingClient.BillingResponseCode.OK) {
          Toast.makeText(this@DownloadActivity, "Connected", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(this@DownloadActivity, "Can not connect", Toast.LENGTH_SHORT).show()
        }
      }
    })
  }


  override fun onLanguageSelected(languageData: LanguageData) {
    addFragment(SelectBookByTheLanguageFragment.newInstance(languageData))
  }

  override fun downloadRequested() {
    //doPayment()
    (supportFragmentManager.findFragmentById(R.id.fragment) as SelectBookByTheLanguageFragment).downloadBook()
  }

  private fun doPayment() {
    if (billingClient.isReady) {
      val skuList = listOf("holysignal")
      val params = SkuDetailsParams.newBuilder()
          .setSkusList(skuList)
          .setType(BillingClient.SkuType.INAPP)
          .build()

      billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailList ->
        if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
          val flowParams = BillingFlowParams.newBuilder()
              .setSkuDetails(skuDetailList[0])
              .build()
          val responseCode = billingClient.launchBillingFlow(this, flowParams)
          try {
            (supportFragmentManager.findFragmentById(R.id.fragment) as SelectBookByTheLanguageFragment).downloadBook()
          } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
          }

          Toast.makeText(this@DownloadActivity, "Billing SKU QUERY RESPONSED", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(this@DownloadActivity, "BillingClient.BillingResponseCode ERROR", Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      Toast.makeText(this@DownloadActivity, "ERROR BillingClient IS NOT READY", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {
    //do nothing
  }
}

