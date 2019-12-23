package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.di.module.SearchWordInAyahsFragmentModule
import com.mguven.holysignal.extension.newTextAfterTextChanged
import com.mguven.holysignal.extension.removeBoxBrackets
import com.mguven.holysignal.extension.removeBoxBracketsAndPutSpaceAfterComma
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.android.synthetic.main.search_word_in_ayahs.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class SearchWordInAyahsFragment : BaseDialogFragment() {

  @Inject
  lateinit var cache: ApplicationCache

  override fun getLayoutResource() = R.layout.search_word_in_ayahs

  private lateinit var holyBookViewModel: HolyBookViewModel

  private var listener: OnFragmentInteractionListener? = null

  companion object {
    fun newInstance() = SearchWordInAyahsFragment().apply {
    }
  }

  private fun inject(application: TheApplication) {
    application
        .applicationComponent
        .plus(SearchWordInAyahsFragmentModule(this))
        .inject(this)
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.let {
      holyBookViewModel = (activity as AbstractBaseActivity).getViewModel(HolyBookViewModel::class.java)
    }

    val searchResult = cache.getAyahSearchResult()
    etWord.setText(searchResult?.keywords.removeBoxBracketsAndPutSpaceAfterComma())
    btnSearch.isEnabled = !searchResult?.keywords.isNullOrBlank()

    etWord.newTextAfterTextChanged { it ->
      whenNotNull(it) { btnSearch.isEnabled = !it.toString().isNullOrEmpty() }
    }

    rbWord.setOnClickListener {
      btnSearch.text = getString(R.string.search)
      etWord.inputType = InputType.TYPE_CLASS_TEXT
    }

    rbAyahNo.setOnClickListener {
      etWord.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_NUMBER
      btnSearch.text = getString(R.string.go)
    }

    btnSearch.setOnClickListener {
      lifecycleScope.launch {
        if (radiogroup.checkedRadioButtonId == R.id.rbWord) {
          val words = etWord.text.toString().removeBoxBrackets().split(",").toMutableSet()
          listener?.onSearchWordEntered(words)
        } else {
          if (etWord.text.toString().toIntOrNull() != null) {
            listener?.onSearchAyahNoEntered(etWord.text.toString().toInt())
          } else {
            Toast.makeText(activity, R.string.not_an_ayah_number, Toast.LENGTH_SHORT).show()
          }
        }
        etWord.setText("")
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      listener = context
      inject(context.applicationContext as TheApplication)
    } else {
      throw RuntimeException("$context must be implement OnFragmentInteractionListener")
    }
  }


  interface OnFragmentInteractionListener {
    fun onSearchWordEntered(words: MutableSet<String>)
    fun onSearchAyahNoEntered(ayahNo: Int)
  }
}