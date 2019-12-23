package com.mguven.holysignal.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.entity.BooksBySelectedLanguageData
import com.mguven.holysignal.db.entity.LanguageData
import com.mguven.holysignal.extension.bold
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.ui.adapter.SearchableSpinnerAdapter
import com.mguven.holysignal.viewmodel.DownloadViewModel
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.coroutines.launch


class SelectBookByTheLanguageFragment : AbstractBaseFragment() {

  private lateinit var downloadViewModel: DownloadViewModel
  private lateinit var languageData: LanguageData
  private lateinit var list: List<BooksBySelectedLanguageData>
  private lateinit var tvLanguage: TextView
  private lateinit var tvProgress: TextView
  private lateinit var btnDownload: AppCompatButton
  private lateinit var progress: ProgressBar
  private lateinit var spBook: SearchableSpinner
  private var isDownloadingStarted = false
  private var listener: BookListener? = null

  companion object {
    private const val LANGUAGE_DATA = "LANGUAGE_DATA"
    fun newInstance(languageData: LanguageData?) = SelectBookByTheLanguageFragment().apply {
      arguments =
          Bundle().apply { putSerializable(LANGUAGE_DATA, languageData) }
    }
  }


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.select_book_by_the_language_fragment, container, false)
    tvLanguage = view.findViewById(R.id.tvLanguage)
    tvProgress = view.findViewById(R.id.tvProgress)
    btnDownload = view.findViewById(R.id.btnDownload)
    progress = view.findViewById(R.id.progress)
    spBook = view.findViewById(R.id.spBook)
    isDownloadingStarted = false
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    arguments?.let {
      languageData = it.getSerializable(LANGUAGE_DATA) as LanguageData
    }
    tvLanguage.bold(getString(R.string.selected_language_is, languageData.originalVersion), languageData.originalVersion)
    updateSpannable()
    arrangePercentage()


    btnDownload.setOnClickListener {
      listener?.downloadRequested()
    }
  }


  fun downloadBook() {
    if (!isDownloadingStarted && list.isNotEmpty()) {
      isDownloadingStarted = true
      downloadViewModel.download(list[spBook.selectedItemPosition].Id, ConstantVariables.TOP_TEXT)
    }
  }

  private fun arrangePercentage() {
    (activity?.application as TheApplication).cache.downloadedTopSurahTranslate.observe(activity as LifecycleOwner, Observer<IntArray> {
      percentageSurahTranslate(it)
    })

    (activity?.application as TheApplication).cache.downloadedTopSurah.observe(activity as LifecycleOwner, Observer<Int> {
      percentageDownload(it)
    })
  }

  private fun updateSpannable(selectedItem: Int = 0) {
    spBook.setTitle(getString(R.string.book_selection))
    lifecycleScope.launch {
      list = downloadViewModel.getBooksByTheSelectedLanguage(languageData.Id)
      spBook.adapter = SearchableSpinnerAdapter(activity as Context, R.layout.status_item, list.map { it.name }, selectedItem)
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.let {
      downloadViewModel = (activity as AbstractBaseActivity).getViewModel(DownloadViewModel::class.java)
      if (context is BookListener) {
        listener = context
      } else {
        throw RuntimeException("$context must implement BookListener")
      }
    }

  }

  private fun percentageSurahTranslate(it: IntArray?) {
    btnDownload.visibility = View.GONE
    it?.let {
      progress.max = it[0]
      val isDone = (it[0]) == it[1]
      progress.progress = it[1]
      val percentage = calculatePercentage(it[1], it[0])
      tvProgress.text = if (isDone)
        btnDownload.context.getString(R.string.download_finished)
      else
        btnDownload.context.getString(R.string.downloading, percentage)

      //btnDownload.visibility = if(isDone) View.VISIBLE else View.GONE
      progress.visibility = if (isDone) View.GONE else View.VISIBLE
      tvProgress.visibility = if (isDone) View.GONE else View.VISIBLE
      spBook.isEnabled = isDone
      Log.e("BBB", "${(it[0])} -- ${it[1]}")
    }
  }

  private fun percentageDownload(it: Int?) {
    btnDownload.visibility = View.GONE
    it?.let {
      val percentage = calculatePercentage(it, ConstantVariables.MAX_SURAH_NUMBER)
      progress.max = ConstantVariables.MAX_SURAH_NUMBER
      progress.progress = it
      tvProgress.text = if (it == ConstantVariables.MAX_SURAH_NUMBER)
        btnDownload.context.getString(R.string.download_finished)
      else
        btnDownload.context.getString(R.string.downloading, percentage)
      val isDone = it == ConstantVariables.MAX_SURAH_NUMBER
      btnDownload.visibility = if (isDone) View.VISIBLE else View.GONE
      tvProgress.visibility = if (isDone) View.GONE else View.VISIBLE
      spBook.isEnabled = isDone
      if (progress.visibility == View.VISIBLE && percentage == 100) {
        FlowController.launchMainActivity(btnDownload.context)
      }
      progress.visibility = if (isDone) View.GONE else View.VISIBLE
    }
  }

  private fun calculatePercentage(number: Int, total: Int): Int = (number * 100) / total

  interface BookListener {
    fun downloadRequested()
  }


}