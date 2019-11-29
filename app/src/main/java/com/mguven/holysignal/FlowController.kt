package com.mguven.holysignal

import android.content.Context
import android.content.Intent
import com.mguven.holysignal.constant.BundleKey
import com.mguven.holysignal.model.Article
import com.mguven.holysignal.ui.ArticleDetailActivity
import com.mguven.holysignal.ui.LoginActivity
import com.mguven.holysignal.ui.MainActivity
import com.mguven.holysignal.ui.SignupActivity

class FlowController {

  companion object {
    fun launchArticleDetailActivity(context: Context, article: Article) {
      context.startActivity(
          Intent(context, ArticleDetailActivity::class.java)
              .putExtra(BundleKey.ARTICLE, article)
      )
    }

    fun launchMainActivity(context: Context, clearTask: Boolean = false) {
      val i = Intent(context, MainActivity::class.java)
      if (clearTask) {
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      context.startActivity(i)
    }

    fun launchLoginActivity(context: Context) {
      context.startActivity(
          Intent(context, LoginActivity::class.java)
      )
    }

    fun launchSignUpActivity(context: Context) {
      context.startActivity(
          Intent(context, SignupActivity::class.java)
      )
    }

  }

}