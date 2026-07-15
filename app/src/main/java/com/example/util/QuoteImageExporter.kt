package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.example.ui.theme.CardTheme
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object QuoteImageExporter {
    private const val TAG = "QuoteImageExporter"

    /**
     * Generates a beautiful bitmap representing the Quote Card.
     * Dimensions: 1080x1080 (Square) or 1080x1350 (Portrait)
     */
    fun generateQuoteBitmap(
        quoteText: String,
        category: String,
        theme: CardTheme,
        fontStyle: String,
        alignment: String,
        isPortrait: Boolean = false
    ): Bitmap {
        val width = 1080
        val height = if (isPortrait) 1350 else 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw background gradient
        val paint = Paint()
        val c1 = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.backgroundColors[0].hashCode()))
        val c2 = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.backgroundColors[1].hashCode()))
        
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            c1, c2,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Clear shader and prepare text paint
        paint.shader = null
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.textColor.hashCode()))
            textSize = 52f
            typeface = when (fontStyle.lowercase()) {
                "monospace" -> Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                "sans" -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                else -> Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
        }

        // 3. Align setup
        val layoutAlign = when (alignment.lowercase()) {
            "left" -> Layout.Alignment.ALIGN_NORMAL
            "right" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        // 4. Wrap & format Quote Text
        val margin = 120
        val maxTextWidth = width - (margin * 2)

        val quoteLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(quoteText, 0, quoteText.length, textPaint, maxTextWidth)
                .setAlignment(layoutAlign)
                .setLineSpacing(15f, 1.1f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                quoteText, textPaint, maxTextWidth,
                layoutAlign, 1.1f, 15f, false
            )
        }

        // 5. Draw decorative quote marks (aesthetic and modern)
        val quoteMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.accentColor.hashCode()))
            alpha = 40 // Transparent
            textSize = 220f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        
        // Draw open quotation mark near top left
        canvas.drawText("“", 80f, 260f, quoteMarkPaint)

        // 6. Draw Quote Text (centered vertically in card)
        val textHeight = quoteLayout.height
        val startY = (height - textHeight) / 2f
        canvas.save()
        canvas.translate(margin.toFloat(), startY)
        quoteLayout.draw(canvas)
        canvas.restore()

        // 7. Draw Category Tag near the top
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.accentColor.hashCode()))
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.1f
        }
        val tagText = category.uppercase()
        val tagBounds = Rect()
        tagPaint.getTextBounds(tagText, 0, tagText.length, tagBounds)
        val tagX = (width - tagBounds.width()) / 2f
        canvas.drawText(tagText, tagX, 120f, tagPaint)

        // 8. Draw Watermark "Created with QuoteBloom AI" near bottom
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#" + String.format("%06X", 0xFFFFFF and theme.subtitleColor.hashCode()))
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            alpha = 180
        }
        val watermarkText = "Created with QuoteBloom AI"
        val wmBounds = Rect()
        watermarkPaint.getTextBounds(watermarkText, 0, watermarkText.length, wmBounds)
        val wmX = (width - wmBounds.width()) / 2f
        canvas.drawText(watermarkText, wmX, height - 90f, watermarkPaint)

        return bitmap
    }

    /**
     * Saves the quote card bitmap to the user's photo gallery / pictures folder.
     */
    fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String = "QuoteBloom_${System.currentTimeMillis()}"
    ): Uri? {
        val resolver = context.contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuoteBloom")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        try {
            val uri = resolver.insert(imageCollection, contentValues) ?: return null
            resolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            return uri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image to gallery", e)
            return null
        }
    }
}
