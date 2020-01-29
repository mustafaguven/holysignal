@file:Suppress("UNCHECKED_CAST")

package com.mguven.holysignal.extension

import android.view.View

fun <T> View.findBy(viewId: Int, clz: Class<T>): T {
  return this.findViewById<View>(viewId) as T
}

fun View.visibilityByIfCollectionHasItems(collection: Collection<*>?, state: Int = View.VISIBLE) {
  val alternate = when (state) {
    View.VISIBLE -> View.GONE
    else -> View.VISIBLE
  }
  this.visibility = if (collection != null && collection.isNotEmpty()) state else alternate
}