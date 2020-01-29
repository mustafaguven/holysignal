package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Edition")
data class EditionData(@PrimaryKey(autoGenerate = true) var Id: Int,
                       @ColumnInfo(name = "identifier") var identifier: String,
                       @ColumnInfo(name = "language") var language: String,
                       @ColumnInfo(name = "name") var name: String,
                       @ColumnInfo(name = "englishName") var englishName: String,
                       @ColumnInfo(name = "format") var format: String,
                       @ColumnInfo(name = "type") var type: String
)