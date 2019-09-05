package com.mguven.holysignal.ui

import android.os.Bundle
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.BundleKey
import com.mguven.holysignal.extension.hoursAgo
import com.mguven.holysignal.extension.loadUrl
import com.mguven.holysignal.model.Article
import kotlinx.android.synthetic.main.expanded_article_item.*

class ArticleDetailActivity : AbstractBaseActivity() {

  val article by lazy {
    (intent.getSerializableExtra(BundleKey.ARTICLE) as Article)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_article_detail)

    image.loadUrl(article.urlToImage)
    tvTitle.text = article.title
    tvDate.text = article.publishedAt?.hoursAgo()
    tvContent.text = article.content
    tvFrom.text = article.author
  }

}