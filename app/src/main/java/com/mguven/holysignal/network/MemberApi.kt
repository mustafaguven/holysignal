package com.mguven.holysignal.network


import com.mguven.holysignal.model.response.MemberEntity
import com.mguven.holysignal.model.request.RequestMemberSession
import retrofit2.http.Body
import retrofit2.http.POST

interface MemberApi {

  @POST("member")
  suspend fun getMemberSessionNo(@Body body: RequestMemberSession): MemberEntity

}
