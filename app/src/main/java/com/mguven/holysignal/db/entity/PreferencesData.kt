package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Preferences")
data class PreferencesData(@PrimaryKey(autoGenerate = true) var Id: Int,
                           @ColumnInfo(name = "topTextEditionId") var topTextEditionId: Int,
                           @ColumnInfo(name = "bottomTextEditionId") var bottomTextEditionId: Int,
                           @ColumnInfo(name = "userName") var userName: String,
                           @ColumnInfo(name = "password") var password: String,
                           @ColumnInfo(name = "displayMode") var displayMode: Int,
                           @ColumnInfo(name = "playMode") var playMode: Int
)