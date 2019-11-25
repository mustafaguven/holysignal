package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Language")
data class LanguageData(@PrimaryKey(autoGenerate = true) var Id: Int,
                        @ColumnInfo(name = "abbreviation") var abbreviation: String,
                        @ColumnInfo(name = "englishVersion") var englishVersion: String,
                        @ColumnInfo(name = "originalVersion") var originalVersion: String
)