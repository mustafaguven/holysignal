package com.mguven.holysignal.network


import com.mguven.holysignal.model.request.RequestAddNote
import com.mguven.holysignal.model.request.RequestInsertVoter
import com.mguven.holysignal.model.request.RequestRemoveNote
import com.mguven.holysignal.model.response.AddNoteEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
import com.mguven.holysignal.model.response.InsertVoterEntity
import com.mguven.holysignal.model.response.RemoveNoteEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotesApi {

  @GET("notes")
  suspend fun getNotesByAyahNumber(@Query("ayahNumber") ayahNumber: Int): GetNotesByAyahNumberEntity

  @POST("notes/insertvoter")
  suspend fun insertVoter(@Body request: RequestInsertVoter): InsertVoterEntity

  @POST("notes/addnote")
  suspend fun addNote(@Body request: RequestAddNote): AddNoteEntity

  @POST("notes/removenote")
  suspend fun removeNote(@Body request: RequestRemoveNote): RemoveNoteEntity
}
