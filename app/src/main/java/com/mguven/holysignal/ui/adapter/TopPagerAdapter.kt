package com.mguven.holysignal.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mguven.holysignal.R
import kotlinx.android.synthetic.main.top_pager_layout.view.*

class TopPagerAdapter : RecyclerView.Adapter<TopPagerAdapter.CategoryViewHolder>() {
    var list: List<String> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(parent)
    }
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }
    fun setItem(list: List<String>) {
        this.list = list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = list.size

    inner class CategoryViewHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        constructor(parent: ViewGroup) :
                this(LayoutInflater.from(parent.context).inflate(R.layout.top_pager_layout, parent, false))
        fun bind(text: String) {
            itemView.tvTopPagerText.text = text
        }
    }
}