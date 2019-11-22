package com.mguven.holysignal.ui.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mguven.holysignal.R
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.db.entity.EditionAdapterData

class EditionAdapter(context: Context,
                     textViewResourceId: Int, private val list: List<EditionAdapterData>,
                     var res: Resources) : ArrayAdapter<EditionAdapterData>(context, textViewResourceId, list) {
  private var currRowVal: EditionAdapterData? = null
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
    if(currRowVal!!.max != ConstantVariables.MAX_AYAH_NUMBER){
      label.text = label.context.getString(R.string.limited_version, currRowVal!!.key)
      label.setTextColor(Color.GRAY)
    } else {
      label.text = currRowVal!!.key
    }

    return row
  }
}