package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class EditionAdapterData(@ColumnInfo(name = "key") var key: String,
                              @ColumnInfo(name="value") var value: Int
)