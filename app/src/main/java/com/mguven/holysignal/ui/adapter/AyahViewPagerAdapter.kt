package com.mguven.holysignal.ui.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.mguven.holysignal.R
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.extension.highlighted
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.extension.setEmpty
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.coroutines.launch

class AyahViewPagerAdapter(val lifecycleScope: LifecycleCoroutineScope,
                           val holyBookViewModel: HolyBookViewModel,
                           val cache: ApplicationCache) :
    RecyclerView.Adapter<AyahViewPagerAdapter.ViewHolder>() {

  var ayahSet: Set<Int>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      ViewHolder(
          LayoutInflater.from(parent.context).inflate(
              R.layout.ayah_view_adapter,
              parent,
              false
          )
      )

  override fun getItemCount() = ayahSet?.size ?: 0

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(ayahSet!!.elementAt(position))
  }

  fun updateAyahSet(ayahSet: MutableSet<Int>) {
    this.ayahSet = ayahSet
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvAyahNumber = view.findViewById<TextView>(R.id.tvAyahNumber)
    private val tvAyahTopText = view.findViewById<TextView>(R.id.tvAyahTopText)
    private val tvViewingCount = view.findViewById<TextView>(R.id.tvViewingCount)
    private val tvAyahBottomText = view.findViewById<TextView>(R.id.tvAyahBottomText)
    fun bind(ayahNumber: Int) {
      getAyahTopText(ayahNumber)
      getAyahBottomText(ayahNumber)
    }

    private fun getAyahTopText(ayahNumber: Int) {
      lifecycleScope.launch {
        val list = holyBookViewModel.getAyahTopText(ayahNumber)
        if (list.isNotNullAndNotEmpty()) {
          getViewingCount(ayahNumber)
          list.forEach {
            cache.updateLastShownAyah(it)
            tvAyahNumber.text = "(${it.meaning})\n${it.surahNameByLanguage} : ${it.numberInSurah}"
            tvAyahTopText.highlighted("<b>${it.language}:</b> ${it.ayahText}", cache.getAyahSearchResult()?.keywords)
          }
        } else {
          tvAyahTopText.text = tvAyahTopText.context.getString(R.string.ayah_not_found_on_primary_book)
          tvAyahNumber.setEmpty()
          tvViewingCount.setEmpty()
        }
      }
    }

    private fun getAyahBottomText(ayahNumber: Int) {
      if (cache.hasSecondLanguageSupport()) {
        tvAyahBottomText.visibility = View.VISIBLE
        lifecycleScope.launch {
          val list = holyBookViewModel.getAyahBottomText(ayahNumber)
          if (list.isNotNullAndNotEmpty()) {
            tvAyahBottomText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            list.forEach {
              tvAyahBottomText.highlighted("<b>${it.language}:</b> ${it.ayahText}")
            }
          } else {
            tvAyahBottomText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvAyahBottomText.text = tvAyahBottomText.context.getString(R.string.ayah_not_found_on_alternative_book)
          }
        }
      } else {
        tvAyahBottomText.visibility = View.GONE
      }
    }

    private fun getViewingCount(ayahNumber: Int) {
      lifecycleScope.launch {
        val count = holyBookViewModel.getViewingCount(ayahNumber)
        tvViewingCount.text = tvViewingCount.context.getString(R.string.total_viewing_count, count)
      }
    }

  }


}