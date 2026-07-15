package com.example.data

import kotlinx.coroutines.flow.Flow

class QuoteRepository(private val savedQuoteDao: SavedQuoteDao) {
    val allSavedQuotes: Flow<List<SavedQuote>> = savedQuoteDao.getAllQuotes()

    suspend fun saveQuote(quote: SavedQuote): Long {
        return savedQuoteDao.insertQuote(quote)
    }

    suspend fun updateQuote(quote: SavedQuote) {
        savedQuoteDao.updateQuote(quote)
    }

    suspend fun deleteQuote(quote: SavedQuote) {
        savedQuoteDao.deleteQuote(quote)
    }

    suspend fun deleteQuoteById(id: Int) {
        savedQuoteDao.deleteQuoteById(id)
    }
}
