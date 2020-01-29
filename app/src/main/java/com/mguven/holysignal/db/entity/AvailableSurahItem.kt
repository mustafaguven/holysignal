package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo

data class AvailableSurahItem(@ColumnInfo(name = "key") var key: String,
                              @ColumnInfo(name = "value") var value: Int,
                              @ColumnInfo(name = "min") var min: Int,
                              @ColumnInfo(name = "max") var max: Int
)