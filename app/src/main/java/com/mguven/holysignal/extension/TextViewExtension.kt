package com.mguven.holysignal.extension

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.core.text.HtmlCompat

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

fun TextView.setEmpty() {
  this.text = ""
}
