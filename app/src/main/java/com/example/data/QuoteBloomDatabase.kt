package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedQuote::class], version = 1, exportSchema = false)
abstract class QuoteBloomDatabase : RoomDatabase() {
    abstract fun savedQuoteDao(): SavedQuoteDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteBloomDatabase? = null

        fun getDatabase(context: Context): QuoteBloomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteBloomDatabase::class.java,
                    "quotebloom_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
