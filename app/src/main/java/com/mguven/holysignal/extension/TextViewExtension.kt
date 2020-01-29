package com.mguven.holysignal.extension

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.mguven.holysignal.R

fun TextView.highlighted(text: String, highlightKeywords: String? = "") {
  var newText = text
  if (!highlightKeywords.isNullOrEmpty()) {
    highlightKeywords.removeBoxBrackets().split(",").forEach { word ->
      newText = newText.replace(word, "<span style='background-color:#FFFF00; color:#000;'><b>${word}</b></span>")
          .replace(word.capitalize(), "<span style='background-color:#FFFF00; color:#000;'><b>${word.capitalize()}</b></span>")
    }
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    this.setText(Html.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
  } else {
    this.setText(Html.fromHtml(newText), TextView.BufferType.SPANNABLE)
  }
}

fun TextView.bold(text: String, boldWord: String) {
  var newText = text
  if (!boldWord.isNullOrEmpty()) {
    newText = text.replace(boldWord, "<span><b>${boldWord}</b></span>")
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    this.setText(Html.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
  } else {
    this.setText(Html.fromHtml(newText), TextView.BufferType.SPANNABLE)
  }
}

fun TextView.setEmpty() {
  this.text = ""
}


fun TextView.withHighlightRaw(text: String) {
  this.setTextColor(ContextCompat.getColor(context, R.color.black))
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    this.setText(Html.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
  } else {
    this.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE)
  }
}