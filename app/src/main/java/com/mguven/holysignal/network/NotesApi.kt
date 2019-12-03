package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestInsertVoter
import com.mguven.holysignal.model.response.InsertVoterEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotesApi {

  @GET("notes")
  suspend fun getNotesByAyahNumber(@Query("ayahNumber") ayahNumber: Int): GetNotesByAyahNumberEntity

  @POST("notes/insertvoter")
  suspend fun insertVoter(@Body request: RequestInsertVoter): InsertVoterEntity

}
