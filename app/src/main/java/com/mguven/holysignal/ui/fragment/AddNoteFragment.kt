package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.R
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.add_note_fragment.*
import kotlinx.coroutines.launch

class AddNoteFragment : BaseDialogFragment() {

  override fun getLayoutResource() = R.layout.add_note_fragment

  private lateinit var holyBookViewModel: HolyBookViewModel

  private var listener: OnFragmentInteractionListener? = null
  private var noteId = Int.MIN_VALUE

  companion object {
    private const val NOTE_ID = "NOTE_ID"
    fun newInstance(noteId: Int?) = AddNoteFragment().apply {
      arguments =
          Bundle().apply { putInt(NOTE_ID, noteId ?: Int.MIN_VALUE) }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
    }

    btnSave.setOnClickListener {
      lifecycleScope.launch {
        val insertNo = holyBookViewModel.upsertNote(noteId, etNote.text.toString())
        listener?.onNoteInserted(insertNo)
      }
    }

    arguments?.let {
      noteId = it.getInt(NOTE_ID)
    }

    getNote()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      listener = context
    } else {
      throw RuntimeException("$context must be implement OnFragmentInteractionListener")
    }
  }

  private fun getNote() {
    if (noteId > Int.MIN_VALUE) {
      lifecycleScope.launch {
        val list = holyBookViewModel.getNoteById(noteId)
        if (list.isNotEmpty()) {
          etNote.setText(list[0].content)
        }
      }
    }
  }

  interface OnFragmentInteractionListener {
    fun onNoteInserted(insertNo: Long)
  }
}