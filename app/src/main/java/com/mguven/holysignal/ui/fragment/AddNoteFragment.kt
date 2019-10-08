package com.mguven.holysignal.ui.fragment

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.mguven.holysignal.R
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.viewmodel.HolyBookViewModel

class AddNoteFragment : DialogFragment() {

  private lateinit var holyBookViewModel: HolyBookViewModel

  companion object {
    fun newInstance() = AddNoteFragment()
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
}