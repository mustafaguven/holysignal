package com.mguven.holysignal.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.constant.ConstantVariables
import com.mguven.holysignal.extension.parseDateToString
import com.mguven.holysignal.inline.whenNotNull
import com.mguven.holysignal.model.NoteEntity
import com.mguven.holysignal.model.NoteResponseEntity
import kotlinx.android.synthetic.main.note_adapter_item.view.*

class NotesAdapter(val ayahNumber: Int, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

  companion object {
    const val NO_SELECTED_ID = -1
  }

  private var result: NoteResponseEntity? = null
  val voteObservable = MutableLiveData<IntArray>()
  val removeObservable = MutableLiveData<Int>()
  val cache = (context.applicationContext as TheApplication).cache
  var voteCount = 0
  var selectedId = NO_SELECTED_ID

  override fun getItemCount(): Int {
    return if (result == null) 0 else result!!.notes.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(LayoutInflater.from(context).inflate(R.layout.note_adapter_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = result!!.notes[position]
    holder.tvName.text = "${item.name}: ${item.note}"
    holder.tvDate.text = item.date.parseDateToString()
    holder.tvVoteCount.text = item.voteCount.toString()
    holder.ivUpVote.visibility = if (cache.getMemberId() == item.memberId || cache.getMemberId() == ConstantVariables.GUEST_USER) View.INVISIBLE else View.VISIBLE
    holder.ivDownVote.visibility = if (cache.getMemberId() == item.memberId || cache.getMemberId() == ConstantVariables.GUEST_USER) View.INVISIBLE else View.VISIBLE
    holder.ivRemoveNote.visibility = if (cache.getMemberId() != item.memberId || cache.getMemberId() == ConstantVariables.GUEST_USER) View.GONE else View.VISIBLE

    result!!.myVotes.find {
      item.Id == it.ayahNoteId
    }.apply {
      whenNotNull(this) {
        holder.ivUpVote.setImageResource(if (this!!.vote > 0) R.drawable.ic_thumb_up_alt_24px_selected else R.drawable.ic_thumb_up_alt_24px)
        holder.ivDownVote.setImageResource(if (this.vote < 0) R.drawable.ic_thumb_down_alt_24px_selected else R.drawable.ic_thumb_down_alt_24px)
      }
    }

    holder.ivDownVote.setOnClickListener {
      changeVote(holder, item, -1)
    }

    holder.ivUpVote.setOnClickListener {
      changeVote(holder, item, 1)
    }

    holder.ivRemoveNote.setOnClickListener {
      removeObservable.postValue(item.Id)
    }

    if (position == result!!.notes.size - 1) {
      val layoutParams = (holder.clMain?.layoutParams as? ViewGroup.MarginLayoutParams)
      layoutParams?.bottomMargin = 0
      holder.clMain?.layoutParams = layoutParams
    }
  }

  private fun changeVote(holder: ViewHolder, item: NoteEntity, value: Int) {
    if (cache.getMemberId() != item.memberId) {
      if (selectedId == NO_SELECTED_ID) {
        selectedId = item.Id
        voteCount = holder.tvVoteCount.text.toString().toInt()
      }
      voteObservable.postValue(intArrayOf(item.Id, value))
    }
  }

  fun updateNoteResponse(response: NoteResponseEntity?) {
    this.result = response
  }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val clMain = view.clMain
  val tvName = view.tvName
  val tvDate = view.tvDate
  val tvVoteCount = view.tvVoteCount
  val ivUpVote = view.ivUpVote
  val ivDownVote = view.ivDownVote
  val ivRemoveNote = view.ivRemoveNote
}