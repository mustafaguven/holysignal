@file:Suppress("UNCHECKED_CAST")

package com.mguven.holysignal.extension

import android.view.View

fun <T> View.findBy(viewId: Int, clz: Class<T>): T {
  return this.findViewById<View>(viewId) as T
}
