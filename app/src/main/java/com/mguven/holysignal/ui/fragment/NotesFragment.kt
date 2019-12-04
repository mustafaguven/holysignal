package com.mguven.holysignal.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.model.response.AddNoteEntity
import com.mguven.holysignal.model.response.InsertVoterEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
import com.mguven.holysignal.model.response.RemoveNoteEntity
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.adapter.NotesAdapter
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.notes_fragment.*

class NotesFragment : BaseDialogFragment() {

  override fun getLayoutResource() = R.layout.notes_fragment
  lateinit var adapter: NotesAdapter

  private lateinit var holyBookViewModel: HolyBookViewModel
  private var ayahNumber = Int.MIN_VALUE

  companion object {
    private const val AYAH_NUMBER = "AYAH_NUMBER"
    fun newInstance(ayahNumber: Int) = NotesFragment().apply {
      arguments =
          Bundle().apply { putInt(AYAH_NUMBER, ayahNumber) }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    rvNotes.layoutManager = LinearLayoutManager(activity)

    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
      btnAddVote.visibility = if(holyBookViewModel.cache.getMemberId() == ConstantVariables.GUEST_USER) View.GONE else View.VISIBLE
      etNote.visibility = if(holyBookViewModel.cache.getMemberId() == ConstantVariables.GUEST_USER) View.GONE else View.VISIBLE
    }

    arguments?.let {
      ayahNumber = it.getInt(AYAH_NUMBER)
    }

    retrieveAllNotes()

    holyBookViewModel.allNotesFromCloud.observe(this, Observer<GetNotesByAyahNumberEntity> { list ->
      if(list.data != null) {
        if(list.data.isEmpty()){
          tvEmpty.text = getString(R.string.no_notes_found)
          tvEmpty.visibility = View.VISIBLE
          rvNotes.visibility = View.GONE
          return@Observer
        } else {
          tvEmpty.visibility = View.GONE
          rvNotes.visibility = View.VISIBLE
        }

        adapter = NotesAdapter(ayahNumber, list.data, activity!!)
        rvNotes.adapter = adapter

        adapter.voteObservable.observe(this, Observer<IntArray>{
          val ayahNoteId = it[0]
          val vote = it[1]
          holyBookViewModel.changeAyahNoteVoteCountObserver.observe(this, Observer<InsertVoterEntity>{
            //do nothing
          })
          holyBookViewModel.insertVoter(ayahNoteId, vote)

        })

        adapter.removeObservable.observe(this, Observer<Int> {
          holyBookViewModel.removeNoteObserver.observe(this, Observer<RemoveNoteEntity>{
            retrieveAllNotes()
          })
          holyBookViewModel.removeAyah(it)
        })
      }
    })

    btnAddVote.setOnClickListener {
      btnAddVote.isEnabled = false
      if(etNote.text.toString().isNotEmpty()) {
        holyBookViewModel.addNoteObserver.observe(this, Observer<AddNoteEntity> {
          retrieveAllNotes()
          btnAddVote.isEnabled = true
        })
        holyBookViewModel.addNote(ayahNumber, etNote.text.toString())
      }
    }



  }

  private fun retrieveAllNotes() {
    holyBookViewModel.getNotesByAyahNumber(ayahNumber)
  }


}