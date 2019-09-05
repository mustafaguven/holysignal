package com.mguven.holysignal.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.common.ViewType
import com.mguven.holysignal.common.ViewTypeDelegateAdapter
import com.mguven.holysignal.extension.findBy
import com.mguven.holysignal.extension.hoursAgo
import com.mguven.holysignal.extension.loadUrl
import com.mguven.holysignal.model.Article


class ArticleDelegateAdapter : ViewTypeDelegateAdapter {

  override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder {
    return ArticleHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item, parent, false))
  }

  override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {
    (holder as ArticleHolder).bind(item as Article)
  }

  private inner class ArticleHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    fun bind(article: Article) {
      itemView.findBy(R.id.tvTitle, TextView::class.java).text = article.title
      itemView.findBy(R.id.tvDate, TextView::class.java).text = article.publishedAt?.hoursAgo()
      itemView.findBy(R.id.tvDescription, TextView::class.java).text = article.description
      itemView.findBy(R.id.tvFrom, TextView::class.java).text = article.author ?: ""
      itemView.findBy(R.id.image, ImageView::class.java).loadUrl(article.urlToImage)

      itemView.findBy(R.id.lnMain, LinearLayout::class.java).setOnClickListener {
        FlowController.launchArticleDetailActivity(itemView.context, article)
      }
    }

  }

}
