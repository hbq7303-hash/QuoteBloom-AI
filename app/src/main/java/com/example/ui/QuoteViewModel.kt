package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.data.QuoteBloomDatabase
import com.example.data.QuoteRepository
import com.example.data.SavedQuote
import com.example.ui.theme.CardTheme
import com.example.ui.theme.QuoteThemes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuoteViewModel(
    application: Application,
    private val repository: QuoteRepository
) : AndroidViewModel(application) {

    // --- GENERATOR FORM STATE ---
    var selectedCategory by mutableStateOf("Morning Motivation")
    var selectedTone by mutableStateOf("Motivational")
    var selectedLength by mutableStateOf("Medium") // Short, Medium, Long
    var selectedAudience by mutableStateOf("For Me") // For Me, For a Friend, etc.
    var customIdea by mutableStateOf("")

    // --- ACTIVE GENERATED QUOTE STATE ---
    var generatedQuoteText by mutableStateOf<String?>(null)
    var isGenerating by mutableStateOf(false)
    var generationError by mutableStateOf<String?>(null)
    var loadingText by mutableStateOf("")

    // Active design customizer values
    var activeTheme by mutableStateOf(QuoteThemes.list[0]) // Default to Lavender Dream
    var activeFontStyle by mutableStateOf("Serif") // Serif, Sans, Monospace
    var activeTextAlignment by mutableStateOf("Center") // Left, Center, Right
    var isActiveQuoteSaved by mutableStateOf(false)
    var activeQuoteId by mutableStateOf<Int?>(null)

    // --- SAVED QUOTES LIST FILTERING & SORTING STATE ---
    val searchQuery = MutableStateFlow("")
    val filterCategory = MutableStateFlow("All")
    val sortBy = MutableStateFlow("Newest") // Newest, Oldest, Favorites First

    // Reactive list of saved quotes based on search, filter and sorting
    val filteredSavedQuotes: StateFlow<List<SavedQuote>> = combine(
        repository.allSavedQuotes,
        searchQuery,
        filterCategory,
        sortBy
    ) { quotes, query, filter, sort ->
        var list = quotes

        // Search
        if (query.isNotBlank()) {
            list = list.filter {
                it.text.contains(query, ignoreCase = true) ||
                        it.customIdea.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        // Category Filter
        if (filter != "All") {
            list = list.filter { it.category.equals(filter, ignoreCase = true) }
        }

        // Sorting
        list = when (sort) {
            "Oldest" -> list.sortedBy { it.createdAt }
            "Favorites First" -> list.sortedWith(compareByDescending<SavedQuote> { it.isFavorite }.thenByDescending { it.createdAt })
            else -> list.sortedByDescending { it.createdAt } // Newest
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- ACTION FEEDBACK (TOASTS) ---
    var feedbackMessage by mutableStateOf<String?>(null)

    init {
        // Prepopulate database with some beautiful quotes if empty
        viewModelScope.launch {
            repository.allSavedQuotes.collect { quotes ->
                if (quotes.isEmpty()) {
                    prepopulateDefaultQuotes()
                }
            }
        }
    }

    private suspend fun prepopulateDefaultQuotes() {
        val defaultQuotes = listOf(
            SavedQuote(
                text = "Some days, progress is simply choosing to begin again.",
                category = "Morning Motivation",
                tone = "Calm",
                length = "Short",
                audience = "For Me",
                customIdea = "",
                themeName = "Lavender Dream",
                fontStyle = "Serif",
                textAlignment = "Center",
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 100000
            ),
            SavedQuote(
                text = "Start softly. Even a small step can change the shape of your day.",
                category = "Mindfulness",
                tone = "Gentle",
                length = "Short",
                audience = "For Me",
                customIdea = "",
                themeName = "Ocean Calm",
                fontStyle = "Serif",
                textAlignment = "Center",
                isFavorite = false,
                createdAt = System.currentTimeMillis() - 200000
            ),
            SavedQuote(
                text = "You do not have to earn the kindness you deserve.",
                category = "Self-Love",
                tone = "Gentle",
                length = "Short",
                audience = "For Me",
                customIdea = "",
                themeName = "Blush Bloom",
                fontStyle = "Serif",
                textAlignment = "Center",
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 300000
            ),
            SavedQuote(
                text = "Focus on the next meaningful step, not the entire mountain.",
                category = "Productivity",
                tone = "Motivational",
                length = "Short",
                audience = "For Entrepreneurs",
                customIdea = "",
                themeName = "Sunrise Gold",
                fontStyle = "Sans",
                textAlignment = "Center",
                isFavorite = false,
                createdAt = System.currentTimeMillis() - 400000
            )
        )
        for (quote in defaultQuotes) {
            repository.saveQuote(quote)
        }
    }

    // --- ACTIONS ---

    fun showFeedback(message: String) {
        feedbackMessage = message
    }

    fun clearFeedback() {
        feedbackMessage = null
    }

    fun generateQuote() {
        viewModelScope.launch {
            isGenerating = true
            generationError = null
            isActiveQuoteSaved = false
            activeQuoteId = null

            val loadings = listOf(
                "Finding the right words…",
                "Creating your inspiration…",
                "Turning your moment into meaning…",
                "Sifting through thoughts…",
                "Crafting unique lines…"
            )
            loadingText = loadings.random()

            val result = GeminiApiClient.generateQuote(
                category = selectedCategory,
                tone = selectedTone,
                length = selectedLength,
                audience = selectedAudience,
                customIdea = customIdea
            )

            result.fold(
                onSuccess = { text ->
                    generatedQuoteText = text
                    isGenerating = false
                },
                onFailure = { err ->
                    Loge("QuoteViewModel", "Generation failed", err)
                    generationError = err.localizedMessage ?: "We couldn't create your quote right now. Please try again."
                    isGenerating = false
                }
            )
        }
    }

    fun usePremadeQuote(text: String, category: String, themeId: String) {
        generatedQuoteText = text
        selectedCategory = category
        activeTheme = QuoteThemes.getById(themeId)
        activeFontStyle = "Serif"
        activeTextAlignment = "Center"
        isActiveQuoteSaved = false
        activeQuoteId = null
    }

    fun saveActiveQuote() {
        val text = generatedQuoteText ?: return
        viewModelScope.launch {
            val savedQuote = SavedQuote(
                text = text,
                category = selectedCategory,
                tone = selectedTone,
                length = selectedLength,
                audience = selectedAudience,
                customIdea = customIdea,
                themeName = activeTheme.name,
                fontStyle = activeFontStyle,
                textAlignment = activeTextAlignment,
                isFavorite = false
            )
            val newId = repository.saveQuote(savedQuote)
            activeQuoteId = newId.toInt()
            isActiveQuoteSaved = true
            showFeedback("Quote saved successfully!")
        }
    }

    fun toggleFavorite(quote: SavedQuote) {
        viewModelScope.launch {
            val updated = quote.copy(isFavorite = !quote.isFavorite)
            repository.updateQuote(updated)
            if (activeQuoteId == quote.id) {
                // If it's the currently active quote, keep sync
            }
            showFeedback(if (updated.isFavorite) "Added to favorites!" else "Removed from favorites")
        }
    }

    fun deleteQuote(quote: SavedQuote) {
        viewModelScope.launch {
            repository.deleteQuote(quote)
            if (activeQuoteId == quote.id) {
                isActiveQuoteSaved = false
                activeQuoteId = null
            }
            showFeedback("Quote deleted successfully")
        }
    }

    fun loadQuoteIntoCustomizer(quote: SavedQuote) {
        generatedQuoteText = quote.text
        selectedCategory = quote.category
        selectedTone = quote.tone
        selectedLength = quote.length
        selectedAudience = quote.audience
        customIdea = quote.customIdea
        activeTheme = QuoteThemes.getByName(quote.themeName)
        activeFontStyle = quote.fontStyle
        activeTextAlignment = quote.textAlignment
        isActiveQuoteSaved = true
        activeQuoteId = quote.id
    }

    private fun Loge(tag: String, msg: String, t: Throwable) {
        android.util.Log.e(tag, msg, t)
    }
}

class QuoteViewModelFactory(
    private val application: Application,
    private val repository: QuoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuoteViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
