package com.mguven.holysignal.extension

fun <T : Any> List<T>?.isNotNullAndNotEmpty() = this != null && this.isNotEmpty()