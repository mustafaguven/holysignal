package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.NotesData
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.add_note_fragment.*

class AddNoteFragment : DialogFragment() {

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

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.add_note_fragment, container)
    dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
    dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    dialog!!.setCanceledOnTouchOutside(true)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
    }

    btnSave.setOnClickListener {
      holyBookViewModel.upsertNote(noteId, etNote.text.toString()).observe(this, Observer<Long> { insertNo ->
        listener?.onNoteInserted(insertNo)
      })
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
      holyBookViewModel.getNoteById(noteId).observe(this, Observer<List<NotesData>> { list ->
        if (list.isNotEmpty()) {
          etNote.setText(list[0].content)
        }
      })
    }
  }

  override fun onStart() {
    super.onStart()
    val dialog = dialog
    if (dialog != null) {
      val width = ViewGroup.LayoutParams.MATCH_PARENT
      val height = ViewGroup.LayoutParams.WRAP_CONTENT
      dialog.window!!.setLayout(width, height)
    }
  }

  interface OnFragmentInteractionListener {
    fun onNoteInserted(insertNo: Long)
  }
}