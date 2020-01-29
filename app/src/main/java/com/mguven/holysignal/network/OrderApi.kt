package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestAddOrder
import com.mguven.holysignal.model.response.AddOrderResponseEntity
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApi {

  @POST("order/add")
  suspend fun addOrder(@Body body: RequestAddOrder): ResponseEntity<AddOrderResponseEntity>

}
