package com.mguven.holysignal.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mguven.holysignal.model.Article
import com.mguven.holysignal.network.NewsApi
import io.reactivex.disposables.CompositeDisposable

class ArticlesDataSourceFactory(private val compositeDisposable: CompositeDisposable,
                                private val newsApi: NewsApi)
  : DataSource.Factory<Int, Article>() {

  val articlesDataSourceLiveData = MutableLiveData<ArticlesDataSource>()

  override fun create(): DataSource<Int, Article> {
    val articleDataSource = ArticlesDataSource(newsApi, compositeDisposable)
    articlesDataSourceLiveData.postValue(articleDataSource)
    return articleDataSource
  }
}