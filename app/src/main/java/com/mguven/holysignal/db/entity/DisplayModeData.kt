package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "DisplayMode")
data class DisplayModeData(@PrimaryKey(autoGenerate = true) var Id: Int,
                           @ColumnInfo(name = "key") var key: String,
                           @ColumnInfo(name = "value") var value: Int
)