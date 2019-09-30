package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "AyahSample")
data class AyahSampleData(@PrimaryKey(autoGenerate = true) var Id: Int,
                          @ColumnInfo(name = "editionId") var editionId: Int,
                          @ColumnInfo(name = "surahNumber") var surahNumber: Int,
                          @ColumnInfo(name = "number") var number: Int,
                          @ColumnInfo(name = "text") var text: String,
                          @ColumnInfo(name = "numberInSurah") var numberInSurah: Int,
                          @ColumnInfo(name = "juz") var juz: Int
)