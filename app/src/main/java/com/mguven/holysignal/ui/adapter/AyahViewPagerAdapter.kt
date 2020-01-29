package com.mguven.holysignal.ui.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mguven.holysignal.R
import com.mguven.holysignal.cache.ApplicationCache
import com.mguven.holysignal.extension.highlighted
import com.mguven.holysignal.extension.isNotNullAndNotEmpty
import com.mguven.holysignal.extension.setEmpty
import com.mguven.holysignal.model.AyahMap
import com.mguven.holysignal.ui.AbstractBaseActivity
import com.mguven.holysignal.util.OnSwipeTouchListener
import com.mguven.holysignal.viewmodel.HolyBookViewModel
import kotlinx.coroutines.launch

class AyahViewPagerAdapter(var activity: AbstractBaseActivity?,
                           val lifecycleScope: LifecycleCoroutineScope,
                           val holyBookViewModel: HolyBookViewModel,
                           val cache: ApplicationCache) :
    RecyclerView.Adapter<AyahViewPagerAdapter.ViewHolder>() {

  var listener: MapValueListener? = null
  private var ayahMap: AyahMap? = null


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.ayah_view_adapter,
            parent,
            false
        )
    )
  }

  override fun getItemCount() = ayahMap?.size ?: 0

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(ayahMap!!.getEntry(position)!!.key)
  }

  fun updateAyahSet(map: AyahMap?) {
    this.ayahMap = map
    this.notifyDataSetChanged()
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val adBanner = view.findViewById<AdView>(R.id.adBanner)
    private val tvAyahNumber = view.findViewById<TextView>(R.id.tvAyahNumber)
    private val tvAyahTopText = view.findViewById<TextView>(R.id.tvAyahTopText)
    private val tvAyahBottomText = view.findViewById<TextView>(R.id.tvAyahBottomText)

    fun bind(ayahNumber: Int) {
      getAyahTopText(ayahNumber)
      getAyahBottomText(ayahNumber)
    }

    private val onTouchListener = object : OnSwipeTouchListener(tvAyahTopText.context) {
      override fun onDoubleTap() {
        listener?.onAyahClicked()
      }

      override fun onLongPress() {
        listener?.onAyahLongPressed()
      }
    }

    private fun getAyahTopText(ayahNumber: Int) {
      lifecycleScope.launch {
        val list = holyBookViewModel.getAyahTopText(ayahNumber)
        if (list.isNotNullAndNotEmpty()) {
          list.forEach {
            if (listener != null) {
              ayahMap!![ayahNumber] = it
              listener!!.onMapValueFound(ayahMap!!)
            }
            tvAyahNumber.text = "(${it.meaning})\n${it.surahNameByLanguage} : ${it.numberInSurah} / ${it.endingAyahNumber - it.startingAyahNumber + 1}"
            tvAyahTopText.highlighted("<b>${it.language}:</b> ${it.ayahText}", cache.getAyahSearchResult()?.keywords)
            tvAyahNumber.setOnTouchListener(onTouchListener)
            tvAyahTopText.setOnTouchListener(onTouchListener)
            tvAyahBottomText.setOnTouchListener(onTouchListener)
          }
        } else {
          tvAyahTopText.text = tvAyahTopText.context.getString(R.string.ayah_not_found_on_primary_book)
          tvAyahNumber.setEmpty()
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


  }

  interface MapValueListener {
    fun onMapValueFound(ayahMap: AyahMap)
    fun onAyahClicked()
    fun onAyahLongPressed()
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    listener = activity as MapValueListener
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    listener = null
    activity = null
  }

}