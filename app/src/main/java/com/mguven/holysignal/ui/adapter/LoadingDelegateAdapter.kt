package com.mguven.holysignal.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mguven.holysignal.R
import com.mguven.holysignal.common.ViewType
import com.mguven.holysignal.common.ViewTypeDelegateAdapter

class LoadingDelegateAdapter : ViewTypeDelegateAdapter {

  override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder {
    return LoadingViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.news_loading, parent, false))
  }

  override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {
    val layoutParams = holder.itemView.layoutParams as androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams
    layoutParams.isFullSpan = true
  }

  private inner class LoadingViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}
