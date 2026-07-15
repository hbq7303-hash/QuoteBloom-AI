package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedQuoteDao {
    @Query("SELECT * FROM saved_quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<SavedQuote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SavedQuote): Long

    @Update
    suspend fun updateQuote(quote: SavedQuote)

    @Delete
    suspend fun deleteQuote(quote: SavedQuote)

    @Query("DELETE FROM saved_quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Int)
}
