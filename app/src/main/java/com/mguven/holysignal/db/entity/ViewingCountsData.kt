package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ViewingCounts")
data class ViewingCountsData(@PrimaryKey(autoGenerate = true) var Id: Long,
                             @ColumnInfo(name = "ayahNumber") var ayahNumber: Int,
                             @ColumnInfo(name = "count") var count: Int
)