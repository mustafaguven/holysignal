package com.mguven.holysignal.extension

import android.view.View
import android.widget.ImageView
import com.mguven.holysignal.TheApplication
import com.squareup.picasso.MemoryPolicy

fun ImageView.loadUrl(url: String?) {
  if (url.isNullOrEmpty() || (!url.isNullOrEmpty() && !url!!.startsWith("http"))) {
    this.visibility = View.GONE
  } else {
    (context.applicationContext as TheApplication)
        .picasso.load(url)
        .memoryPolicy(MemoryPolicy.NO_STORE)
        .into(this)
  }

}