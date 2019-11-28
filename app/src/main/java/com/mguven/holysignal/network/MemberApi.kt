package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestMemberSession
import com.mguven.holysignal.model.request.RequestSignIn
import com.mguven.holysignal.model.request.RequestSignUp
import com.mguven.holysignal.model.response.MemberEntity
import com.mguven.holysignal.model.response.SignInEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface MemberApi {

  @POST("member/check")
  suspend fun getMemberSessionNo(@Body body: RequestMemberSession): MemberEntity

  @POST("member/signin")
  suspend fun signIn(@Body body: RequestSignIn) : SignInEntity

  @POST("member/updatesessionno")
  suspend fun updateSessionNo(@Body body: RequestSignIn): SignInEntity

  @POST("member/signup")
  suspend fun save(@Body body: RequestSignUp): SignInEntity

}
