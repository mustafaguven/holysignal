package com.mguven.holysignal.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Favourites")
data class FavouritesData(@PrimaryKey(autoGenerate = true) var Id: Int,
                          @ColumnInfo(name = "ayahNumber") var ayahNumber: Int,
                          @ColumnInfo(name = "editionId") var editionId: Int
)