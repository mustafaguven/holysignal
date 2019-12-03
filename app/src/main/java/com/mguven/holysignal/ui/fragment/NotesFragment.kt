package com.mguven.holysignal.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mguven.holysignal.R
import com.mguven.holysignal.model.response.InsertVoterEntity
import com.mguven.holysignal.model.response.GetNotesByAyahNumberEntity
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
    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
    }

    arguments?.let {
      ayahNumber = it.getInt(AYAH_NUMBER)
    }

    addAdapter()
    holyBookViewModel.allNotesFromCloud.observe(this, Observer<GetNotesByAyahNumberEntity> { list ->
      if(list.data != null) {
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
      }
    })


  }

  private fun addAdapter() {
    rvNotes.layoutManager = LinearLayoutManager(activity)
    holyBookViewModel.getNotesByAyahNumber(ayahNumber)
  }


}