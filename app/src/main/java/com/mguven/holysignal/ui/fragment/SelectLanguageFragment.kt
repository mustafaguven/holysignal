package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.adapter.SearchableSpinnerAdapter
import com.mguven.holysignal.viewmodel.DownloadViewModel
import kotlinx.android.synthetic.main.select_language_fragment.*
import kotlinx.coroutines.launch

class SelectLanguageFragment : AbstractBaseFragment() {

  private lateinit var downloadViewModel: DownloadViewModel
  private lateinit var list: List<LanguageData>
  private lateinit var listener: LanguageListener

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.select_language_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    updateSpannable()
  }

  private fun updateSpannable(selectedItem: Int = 0) {
    spLanguage.setTitle(getString(R.string.language_selection))
    lifecycleScope.launch {
      list = downloadViewModel.getAllLanguages()
      spLanguage.adapter = SearchableSpinnerAdapter(activity as Context, R.layout.status_item, list.map { it.originalVersion }, selectedItem)
    }

    btnOk.setOnClickListener {
      val position = spLanguage.selectedItemPosition
      listener.onLanguageSelected(list[position])
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.let {
      downloadViewModel = (activity as AbstractBaseActivity).getViewModel(DownloadViewModel::class.java)
    }
    if (context is LanguageListener) {
      listener = context
    } else {
      throw RuntimeException("$context must be implement LanguageListener")
    }
  }

  interface LanguageListener {
    fun onLanguageSelected(languageData: LanguageData)
  }

}