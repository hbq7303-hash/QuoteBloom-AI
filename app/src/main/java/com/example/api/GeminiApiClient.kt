package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateQuote(
        category: String,
        tone: String,
        length: String,
        audience: String,
        customIdea: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is empty or placeholder!")
            return@withContext Result.failure(Exception("API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio."))
        }

        val systemPrompt = """
            You are QuoteBloom AI, an expert creator of original, emotionally resonant quotes. Create fresh, non-attributed quotes tailored to the user's requested category, tone, quote length, audience, and custom idea. Do not reuse famous quotes or imitate identifiable living writers, celebrities, song lyrics, books, films, or religious scripture. Do not add an author name. Return only one polished quote. Make it concise, meaningful, natural, and shareable.
        """.trimIndent()

        val userPrompt = """
            Create one original quote.

            Category: $category
            Tone: $tone
            Length: $length
            Audience/context: $audience
            Custom idea: $customIdea

            Requirements:
            - Make it emotionally meaningful and easy to understand.
            - Ensure it is fully original and not attributed to any person.
            - Return only the quote text.
            - Do not use quotation marks around it.
        """.trimIndent()

        try {
            // Build the JSON structure using org.json
            val requestJson = JSONObject()

            // contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", userPrompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // systemInstruction
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            val systemPartObj = JSONObject()
            systemPartObj.put("text", systemPrompt)
            systemPartsArray.put(systemPartObj)
            systemInstructionObj.put("parts", systemPartsArray)
            requestJson.put("systemInstruction", systemInstructionObj)

            // generationConfig
            val generationConfigObj = JSONObject()
            generationConfigObj.put("temperature", 0.75)
            requestJson.put("generationConfig", generationConfigObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Error: Code ${response.code}, Body: $errBody")
                    return@withContext Result.failure(IOException("Failed with code ${response.code}: $errBody"))
                }

                val responseBodyStr = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response body"))

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            var text = parts.getJSONObject(0).optString("text", "")
                            // Clean up quotation marks if any
                            text = text.trim()
                            if (text.startsWith("\"") && text.endsWith("\"")) {
                                text = text.substring(1, text.length - 1)
                            }
                            if (text.startsWith("“") && text.endsWith("”")) {
                                text = text.substring(1, text.length - 1)
                            }
                            return@withContext Result.success(text.trim())
                        }
                    }
                }
                
                return@withContext Result.failure(Exception("Could not extract text from candidates"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return@withContext Result.failure(e)
        }
    }
}
