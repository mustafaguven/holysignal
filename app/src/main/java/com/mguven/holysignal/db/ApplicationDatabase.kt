package com.mguven.holysignal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mguven.holysignal.db.dao.*
import com.mguven.holysignal.db.entity.*
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import java.io.IOException


@Database(entities = [EditionData::class, SurahData::class, AyahSampleData::class,
  FavouritesData::class, PreferencesData::class, DisplayModeData::class, NotesData::class,
  LanguageData::class, SurahTranslateData::class, ViewingCountsData::class], version = 1)

abstract class ApplicationDatabase : RoomDatabase() {

  abstract fun editionDataDao(): EditionDataDao
  abstract fun surahDataDao(): SurahDataDao
  abstract fun ayahSampleDataDao(): AyahSampleDataDao
  abstract fun favouritesDataDao(): FavouritesDataDao
  abstract fun preferencesDataDao(): PreferencesDataDao
  abstract fun displayModeDataDao(): DisplayModeDataDao
  abstract fun notesDataDao(): NotesDataDao
  abstract fun languageDataDao(): LanguageDataDao
  abstract fun surahTranslateDataDao(): SurahTranslateDataDao
  abstract fun viewingCountsDataDao(): ViewingCountsDataDao

  companion object {
    @Volatile
    private var INSTANCE: ApplicationDatabase? = null
    private const val DB_NAME = "holysignal"
    private val LOCK = Any()

    operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
      INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
    }

    private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
        ApplicationDatabase::class.java, DB_NAME).createFromAsset("databases/$DB_NAME.db")
        .build()

    fun destroyInstance() {
      INSTANCE = null
    }
  }

}