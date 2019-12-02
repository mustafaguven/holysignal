package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestAddFavourites
import com.mguven.holysignal.model.request.RequestMemberSession
import com.mguven.holysignal.model.response.AddFavouritesEntity
import com.mguven.holysignal.model.response.GetFavouriteCountByAyahNumberEntity
import com.mguven.holysignal.model.response.SurahEntity
import com.mguven.holysignal.model.response.SurahTranslateResponseEntity
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FavouritesApi {

  @POST("favourites/addorremove")
  suspend fun addFavourite(@Body body: RequestAddFavourites): AddFavouritesEntity

  @GET("favourites/getfavouritecountbyayahnumber")
  suspend fun getFavouriteCountByAyahNumber(@Query("ayahNumber") ayahNumber: Int): GetFavouriteCountByAyahNumberEntity

}
