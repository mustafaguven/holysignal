package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment : DialogFragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(getLayoutResource(), container)
    dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
    dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    dialog!!.setCanceledOnTouchOutside(true)
    return view
  }

  abstract fun getLayoutResource() : Int

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