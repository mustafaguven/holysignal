package com.mguven.holysignal.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mguven.holysignal.R
import com.mguven.holysignal.db.entity.AvailableSurahItem

class AvailableSurahAdapter(context: Context,
                            textViewResourceId: Int, private val list: List<AvailableSurahItem>,
                            private val selectedItem: Int) : ArrayAdapter<AvailableSurahItem>(context, textViewResourceId, list) {
  private var currRowVal: AvailableSurahItem? = null
  private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    return getCustomView(position, convertView, parent)
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    return getCustomView(position, convertView, parent)
  }

  private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
    val row = inflater.inflate(R.layout.status_item, parent, false)
    currRowVal = null
    currRowVal = list[position]
    val label = row.findViewById<TextView>(R.id.spinnerItem)
    label.text = currRowVal!!.key

    if (position == selectedItem) {
      label.setTextColor(ContextCompat.getColor(label.context, R.color.colorAccent))
    }
    return row
  }
}