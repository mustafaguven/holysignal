package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class SurahAyahSampleData(@ColumnInfo(name = "surahNumber") var surahNumber: Int,
                               @ColumnInfo(name = "surahName") var surahName: String,
                               @ColumnInfo(name = "surahEnglishName") var surahEnglishName: String,
                               @ColumnInfo(name = "surahEnglishNameTranslation") var surahEnglishNameTranslation: String,
                               @ColumnInfo(name = "surahRevelationType") var surahRevelationType: String,
                               @ColumnInfo(name = "startingAyahNumber") var startingAyahNumber: Int,
                               @ColumnInfo(name = "endingAyahNumber") var endingAyahNumber: Int,
                               @ColumnInfo(name = "language") var language: String,
                               @ColumnInfo(name = "ayahId") var ayahId: Int,
                               @ColumnInfo(name = "ayahNumber") var ayahNumber: Int,
                               @ColumnInfo(name = "ayahText") var ayahText: String,
                               @ColumnInfo(name = "numberInSurah") var numberInSurah: Int,
                               @ColumnInfo(name = "juz") var juz: Int,
                               @ColumnInfo(name = "noteId") var noteId: Int?,
                               @ColumnInfo(name = "surahNameByLanguage") var surahNameByLanguage: String
)