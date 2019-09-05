package com.mguven.holysignal.network


import com.mguven.holysignal.model.News

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

  @GET("top-headlines?q=a")
  fun getNews(@Query("page") page: Int = 1,
              @Query("pageSize") pageSize: Int): Observable<News>

}
