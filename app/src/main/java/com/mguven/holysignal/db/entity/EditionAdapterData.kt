package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo

data class EditionAdapterData(@ColumnInfo(name = "key") var key: String,
                              @ColumnInfo(name="value") var value: Int
)