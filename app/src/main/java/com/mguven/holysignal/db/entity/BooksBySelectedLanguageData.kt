package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo


data class BooksBySelectedLanguageData(@ColumnInfo(name = "Id") var Id: Int,
                                       @ColumnInfo(name = "name") var name: String)
