package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestAddDownload
import com.mguven.holysignal.model.response.AddDownloadResponseEntity
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface DownloadApi {

  @POST("download/add")
  suspend fun addDownload(@Body body: RequestAddDownload): ResponseEntity<AddDownloadResponseEntity>

}
