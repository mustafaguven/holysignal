package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
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

    btnSearch.setOnClickListener {
      lifecycleScope.launch {
        val words = etWord.text.toString().removeBoxBrackets().split(",").toMutableSet()
        listener?.onSearchWordEntered(words)
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
  }
}