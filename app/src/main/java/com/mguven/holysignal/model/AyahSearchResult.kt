package com.mguven.holysignal.model

data class AyahSearchResult(val list: List<Int>?,
                            val keywords: String,
                            var lastIndex: Int = 0)