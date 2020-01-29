package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Favourites")
data class FavouritesData(@PrimaryKey(autoGenerate = true) var Id: Long,
                          @ColumnInfo(name = "ayahNumber") var ayahNumber: Int
)