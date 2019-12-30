package com.mguven.holysignal.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthenticationService {
  @POST("oauth/request_token")
  fun getRequestToken(@Header("Authorization") oauthHeader: String): Call<ResponseBody>

  @POST("oauth/access_token")
  fun getAccessToken(@Header("Authorization") oauthHeader: String): Call<ResponseBody>
}