package com.mguven.holysignal.network


import com.mguven.holysignal.model.response.SurahEntity

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

  @GET("top-headlines?q=a")
  fun getNews(@Query("page") page: Int = 1,
              @Query("pageSize") pageSize: Int): Observable<SurahEntity>

}
