package com.mguven.holysignal

import android.content.Context
import android.content.Intent
import com.mguven.holysignal.constant.BundleKey
import com.mguven.holysignal.model.Article
import com.mguven.holysignal.ui.ArticleDetailActivity

class FlowController {

  companion object {
    fun launchArticleDetailActivity(context: Context, article: Article) {
      context.startActivity(
          Intent(context, ArticleDetailActivity::class.java)
              .putExtra(BundleKey.ARTICLE, article)
      )
    }

  }

}