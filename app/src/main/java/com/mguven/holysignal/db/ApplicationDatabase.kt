package com.mguven.holysignal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mguven.holysignal.db.dao.AyahSampleDataDao
import com.mguven.holysignal.db.dao.EditionDataDao
import com.mguven.holysignal.db.dao.SurahDataDao
import com.mguven.holysignal.db.entity.AyahSampleData
import com.mguven.holysignal.db.entity.EditionData
import com.mguven.holysignal.db.entity.SurahData

@Database(entities = [EditionData::class, SurahData::class, AyahSampleData::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {

  abstract fun editionDataDao(): EditionDataDao
  abstract fun surahDataDao(): SurahDataDao
  abstract fun ayahSampleDataDao(): AyahSampleDataDao

  companion object {
    @Volatile
    private var INSTANCE: ApplicationDatabase? = null
    const val DB_NAME = "holysignal"
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