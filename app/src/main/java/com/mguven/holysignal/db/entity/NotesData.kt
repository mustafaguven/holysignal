package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Notes")
data class NotesData(@PrimaryKey(autoGenerate = true) var Id: Int,
                     @ColumnInfo(name = "content") var content: String
)