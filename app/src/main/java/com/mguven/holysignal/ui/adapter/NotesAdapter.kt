package com.mguven.holysignal.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.extension.parseDateToString
import com.mguven.holysignal.model.NoteResponseEntity
import kotlinx.android.synthetic.main.note_adapter_item.view.*

class NotesAdapter(val ayahNumber: Int, private val items: List<NoteResponseEntity>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

  val voteObservable = MutableLiveData<IntArray>()
  val cache = (context.applicationContext as TheApplication).cache

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(LayoutInflater.from(context).inflate(R.layout.note_adapter_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = items[position]
    holder.tvName.text = "${item.name}: ${item.note}"
    holder.tvDate.text = item.date.parseDateToString()
    holder.tvVoteCount.text = item.voteCount.toString()
    holder.ivUpVote.visibility = if (cache.getMemberId() == item.memberId) View.INVISIBLE else View.VISIBLE
    holder.ivDownVote.visibility = if (cache.getMemberId() == item.memberId) View.INVISIBLE else View.VISIBLE

    holder.ivDownVote.setOnClickListener {
      changeVote(holder, item, -1)
    }

    holder.ivUpVote.setOnClickListener {
      changeVote(holder, item, 1)
    }
  }

  private fun changeVote(holder: ViewHolder, item: NoteResponseEntity, value: Int) {
    if (cache.getMemberId() != item.memberId) {
      val voteCount = holder.tvVoteCount.text.toString().toInt()
      var newVoteCount = voteCount + value
      if(newVoteCount > 1) newVoteCount = 1
      if(newVoteCount < -1) newVoteCount = -1
      holder.tvVoteCount.text = (newVoteCount).toString()
      voteObservable.postValue(intArrayOf(item.Id, newVoteCount))
    }
  }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val tvName = view.tvName
  val tvDate = view.tvDate
  val tvVoteCount = view.tvVoteCount
  val ivUpVote = view.ivUpVote
  val ivDownVote = view.ivDownVote
}