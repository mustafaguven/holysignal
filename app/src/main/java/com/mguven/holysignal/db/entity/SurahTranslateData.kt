package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "SurahTranslate")
data class SurahTranslateData(@PrimaryKey(autoGenerate = true) var Id: Int,
                              @ColumnInfo(name = "surahNumber") var surahNumber: Int,
                              @ColumnInfo(name = "languageId") var languageId: Int,
                              @ColumnInfo(name = "name") var name: String,
                              @ColumnInfo(name = "meaning") var meaning: String,
                              @ColumnInfo(name = "specification") var specification: String?)
