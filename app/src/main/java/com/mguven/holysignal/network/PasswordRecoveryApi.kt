package com.mguven.holysignal.network


import com.mguven.holysignal.model.SetNewPasswordResponseEntity
import com.mguven.holysignal.model.request.RequestPasswordReset
import com.mguven.holysignal.model.request.RequestSetNewPassword
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface PasswordRecoveryApi {

  @POST("passwordrecovery/reset")
  suspend fun reset(@Body body: RequestPasswordReset): ResponseEntity<Int>

  @POST("passwordrecovery/setnewpassword")
  suspend fun setNewPassword(@Body body: RequestSetNewPassword): ResponseEntity<SetNewPasswordResponseEntity>

}
