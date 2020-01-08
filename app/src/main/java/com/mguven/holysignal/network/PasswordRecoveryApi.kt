package com.mguven.holysignal.network


import com.mguven.holysignal.model.MemberResponseEntity
import com.mguven.holysignal.model.SignInResponseEntity
import com.mguven.holysignal.model.request.RequestMemberSession
import com.mguven.holysignal.model.request.RequestSignIn
import com.mguven.holysignal.model.request.RequestSignUp
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface MemberApi {

  @POST("member/check")
  suspend fun getMemberSessionNo(@Body body: RequestMemberSession): ResponseEntity<MemberResponseEntity>

  @POST("member/signin")
  suspend fun signIn(@Body body: RequestSignIn) : ResponseEntity<SignInResponseEntity>

  @POST("member/updatesessionno")
  suspend fun updateSessionNo(@Body body: RequestSignIn): ResponseEntity<SignInResponseEntity>

  @POST("member/signup")
  suspend fun save(@Body body: RequestSignUp): ResponseEntity<SignInResponseEntity>

}
