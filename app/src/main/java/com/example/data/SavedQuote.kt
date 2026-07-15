package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_quotes")
data class SavedQuote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val category: String,
    val tone: String,
    val length: String,
    val audience: String,
    val customIdea: String,
    val themeName: String,
    val fontStyle: String = "Serif",
    val textAlignment: String = "Center",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
