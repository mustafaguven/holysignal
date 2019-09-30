package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Surah")
data class SurahData(@PrimaryKey(autoGenerate = true) var Id: Int,
                     @ColumnInfo(name = "number") var number: Int,
                     @ColumnInfo(name = "name") var name: String,
                     @ColumnInfo(name = "englishName") var englishName: String,
                     @ColumnInfo(name = "englishNameTranslation") var englishNameTranslation: String,
                     @ColumnInfo(name = "revelationType") var revelationType: String
)