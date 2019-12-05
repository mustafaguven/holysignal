package com.mguven.holysignal.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.model.response.AddNoteEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
import com.mguven.holysignal.model.response.InsertVoterEntity
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



    arguments?.let {
      ayahNumber = it.getInt(AYAH_NUMBER)
    }

  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
      btnAddVote.visibility = if (holyBookViewModel.cache.getMemberId() == ConstantVariables.GUEST_USER) View.GONE else View.VISIBLE
      etNote.visibility = if (holyBookViewModel.cache.getMemberId() == ConstantVariables.GUEST_USER) View.GONE else View.VISIBLE
    }

    adapter = NotesAdapter(ayahNumber, activity!!)
    retrieveAllNotes()

    holyBookViewModel.allNotesFromCloud.observe(viewLifecycleOwner, Observer<GetNotesByAyahNumberEntity> { list ->
      if (list.data != null) {
        if (list.data.notes.isEmpty()) {
          tvEmpty.text = getString(R.string.no_notes_found)
          tvEmpty.visibility = View.VISIBLE
          rvNotes.visibility = View.GONE
          return@Observer
        } else {
          tvEmpty.visibility = View.GONE
          rvNotes.visibility = View.VISIBLE
          adapter.updateNoteResponse(list.data)
        }
        rvNotes.adapter = adapter
      }
    })

    holyBookViewModel.changeAyahNoteVoteCountObserver.observe(this, Observer<InsertVoterEntity> {
      retrieveAllNotes()
    })

    adapter.voteObservable.observe(this, Observer<IntArray> {
      val ayahNoteId = it[0]
      val vote = it[1]
      holyBookViewModel.insertVoter(ayahNoteId, vote)
    })

    adapter.removeObservable.observe(this, Observer<Int> {
      holyBookViewModel.removeAyah(it)
    })

    holyBookViewModel.removeNoteObserver.observe(this, Observer<RemoveNoteEntity> {
      retrieveAllNotes()
    })

    btnAddVote.setOnClickListener {
      if (etNote.text.toString().isNotEmpty()) {
        btnAddVote.isEnabled = false
        holyBookViewModel.addNote(ayahNumber, etNote.text.toString())
      }
    }

    holyBookViewModel.addNoteObserver.observe(viewLifecycleOwner, Observer<AddNoteEntity> {
      etNote.setText("")
      retrieveAllNotes()
      btnAddVote.isEnabled = true
    })
  }

  private fun retrieveAllNotes() {
    holyBookViewModel.getNotesByAyahNumber(ayahNumber)
  }


}