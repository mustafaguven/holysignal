package com.mguven.holysignal.network


import com.mguven.holysignal.model.AddNoteResponseEntity
import com.mguven.holysignal.model.InsertVoterResponseEntity
import com.mguven.holysignal.model.NoteResponseEntity
import com.mguven.holysignal.model.RemoveNoteResponseEntity
import com.mguven.holysignal.model.request.RequestAddNote
import com.mguven.holysignal.model.request.RequestInsertVoter
import com.mguven.holysignal.model.request.RequestRemoveNote
import com.mguven.holysignal.model.response.ResponseEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotesApi {

  @GET("notes")
  suspend fun getNotesByAyahNumber(@Query("memberId") memberId: Int, @Query("ayahNumber") ayahNumber: Int): ResponseEntity<NoteResponseEntity>

  @POST("notes/insertvoter")
  suspend fun insertVoter(@Body request: RequestInsertVoter): ResponseEntity<InsertVoterResponseEntity>

  @POST("notes/addnote")
  suspend fun addNote(@Body request: RequestAddNote): ResponseEntity<AddNoteResponseEntity>

  @POST("notes/removenote")
  suspend fun removeNote(@Body request: RequestRemoveNote): ResponseEntity<RemoveNoteResponseEntity>
}
