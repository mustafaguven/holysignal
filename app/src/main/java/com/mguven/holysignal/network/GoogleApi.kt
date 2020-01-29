package com.mguven.holysignal.network


import com.mguven.holysignal.model.response.GoogleUserProfileResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApi {


  @GET("userinfo")
  suspend fun getUserInfo(@Query("access_token") accessToken: String): GoogleUserProfileResponse

}
