package com.mguven.holysignal.extension

import android.content.res.Configuration


fun androidx.recyclerview.widget.RecyclerView.loadLayoutManager() {
  val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
  this.layoutManager = layoutManager
}

fun androidx.recyclerview.widget.RecyclerView.loadLayoutManagerInBonialStyle() {
  val spanCount = if (this.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
  val layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(spanCount, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL)

  this.layoutManager = layoutManager
}