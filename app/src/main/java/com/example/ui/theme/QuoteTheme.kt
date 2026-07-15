package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class CardTheme(
    val id: String,
    val name: String,
    val backgroundColors: List<Color>,
    val textColor: Color,
    val accentColor: Color,
    val subtitleColor: Color,
    val description: String
)

object QuoteThemes {
    val list = listOf(
        CardTheme(
            id = "lavender_dream",
            name = "Lavender Dream",
            backgroundColors = listOf(Color(0xFFE6E0F9), Color(0xFFF3E7F3), Color(0xFFFDECF2)),
            textColor = Color(0xFF2D2A44),
            accentColor = Color(0xFF6750A4),
            subtitleColor = Color(0xFF5A449A),
            description = "Gentle purple gradients for thoughtful moments"
        ),
        CardTheme(
            id = "sunrise_gold",
            name = "Sunrise Gold",
            backgroundColors = listOf(Color(0xFFFFF1D6), Color(0xFFFFD17B)),
            textColor = Color(0xFF5C3C00),
            accentColor = Color(0xFFD68A00),
            subtitleColor = Color(0xFF8C5E06),
            description = "A warm, radiant blend to light up your day"
        ),
        CardTheme(
            id = "midnight_focus",
            name = "Midnight Focus",
            backgroundColors = listOf(Color(0xFF13111C), Color(0xFF261D38)),
            textColor = Color(0xFFECE6FA),
            accentColor = Color(0xFFB398F6),
            subtitleColor = Color(0xFF9085A8),
            description = "Deep violet and cosmic slate for intense reflection"
        ),
        CardTheme(
            id = "ocean_calm",
            name = "Ocean Calm",
            backgroundColors = listOf(Color(0xFFE1F5FE), Color(0xFF81D4FA)),
            textColor = Color(0xFF014B69),
            accentColor = Color(0xFF0288D1),
            subtitleColor = Color(0xFF026792),
            description = "Refreshing sea breeze and tranquil blue horizons"
        ),
        CardTheme(
            id = "blush_bloom",
            name = "Blush Bloom",
            backgroundColors = listOf(Color(0xFFFFF0F5), Color(0xFFFFC0CB)),
            textColor = Color(0xFF6E183E),
            accentColor = Color(0xFFD81B60),
            subtitleColor = Color(0xFF9C2F5F),
            description = "Soft pink and delicate petals for encouraging self-love"
        ),
        CardTheme(
            id = "forest_growth",
            name = "Forest Growth",
            backgroundColors = listOf(Color(0xFFE8F5E9), Color(0xFFA5D6A7)),
            textColor = Color(0xFF1B4322),
            accentColor = Color(0xFF2E7D32),
            subtitleColor = Color(0xFF386641),
            description = "Earthy greens signifying steady, quiet renewal"
        ),
        CardTheme(
            id = "minimal_white",
            name = "Minimal White",
            backgroundColors = listOf(Color(0xFFFCFCFD), Color(0xFFF2F4F7)),
            textColor = Color(0xFF1F2937),
            accentColor = Color(0xFF6B7280),
            subtitleColor = Color(0xFF4B5563),
            description = "A clean, spacious canvas keeping focus pure"
        ),
        CardTheme(
            id = "sunset_energy",
            name = "Sunset Energy",
            backgroundColors = listOf(Color(0xFFFFECEB), Color(0xFFFF8A80)),
            textColor = Color(0xFF5E0B05),
            accentColor = Color(0xFFD32F2F),
            subtitleColor = Color(0xFF8A1E1E),
            description = "Vibrant Crimson to warm amber for high-motivation beats"
        )
    )

    fun getById(id: String): CardTheme {
        return list.firstOrNull { it.id == id } ?: list[0]
    }

    fun getByName(name: String): CardTheme {
        return list.firstOrNull { it.name == name } ?: list[0]
    }
}
