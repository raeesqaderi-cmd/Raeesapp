package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeCommand(prompt: String): GeminiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResponse(
                action = "NONE",
                parameter = "",
                response = "د ګیمیني API کیلي ورکه ده. مهرباني وکړئ په سکرین کې د ترتیب لارښود تعقیب کړئ.\n\nGemini API key is missing. Please configure it in AI Studio Secrets.",
                language = "ps"
            )
        }

        // Setup system instructions to force structured JSON intent classification
        val systemInstruction = """
            You are "د رئیس ږغ RAEESTALK", an advanced, luxury premium zero-defect multilingual AI Voice Assistant.
            Your job is to analyze the user's voice command and categorize it into on-device actions:
            1. CALL: For placing phone calls. Extract a destination phone number or contact name (e.g., "0799123456" or "احمد").
            2. ALARM: For setting alarms. Extract alarm details (e.g., time like "7:30 AM" or description).
            3. MAP: For opening maps or directions. Extract the location name or address (e.g., "Kabul" or "دبۍ").
            4. SETTINGS: For toggling settings or opening system configuration.
            5. NONE: For general questions, casual conversations, translations, or general knowledge.

            You must ALWAYS return a JSON object with this exact structure:
            {
              "action": "CALL" | "ALARM" | "MAP" | "SETTINGS" | "NONE",
              "parameter": "extracted phone/name/address/time/detail or empty string",
              "response": "A highly elegant verbal response in the same language as the user's input.",
              "language": "ps" | "prs" | "fa" | "en" | "ar" | "ur"
            }

            Language support mapping:
            - ps: Pashto (پښتو)
            - prs: Dari (دري)
            - fa: Farsi (فارسی)
            - en: English
            - ar: Arabic (العربية)
            - ur: Urdu (اردو)

            Automatically detect the language of the user input and respond in that SAME language with a luxurious, polite tone.
            Do NOT include markdown blocks like ```json or any other text before or after the JSON. Return only the raw JSON.
        """.trimIndent()

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Error: ${response.code} $errBody")
                    return@withContext GeminiResponse(
                        action = "NONE",
                        parameter = "",
                        response = "تېروتنه رامنځته شوه. د شبکې اتصال وګورئ.\n\nAn error occurred. Check network connection.",
                        language = "ps"
                    )
                }

                val bodyString = response.body?.string() ?: ""
                Log.d(TAG, "API Response: $bodyString")

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            val cleanText = text.trim()

                            val parsedJson = JSONObject(cleanText)
                            return@withContext GeminiResponse(
                                action = parsedJson.optString("action", "NONE").uppercase(),
                                parameter = parsedJson.optString("parameter", ""),
                                response = parsedJson.optString("response", "No response text"),
                                language = parsedJson.optString("language", "en")
                            )
                        }
                    }
                }

                return@withContext GeminiResponse(
                    action = "NONE",
                    parameter = "",
                    response = "زه په دې نه پوهېږم. مهرباني وکړئ بیا هڅه وکړئ.\n\nI could not understand that. Please try again.",
                    language = "en"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API Call", e)
            return@withContext GeminiResponse(
                action = "NONE",
                parameter = "",
                response = "تېروتنه: ${e.localizedMessage}\n\nError: ${e.localizedMessage}",
                language = "en"
            )
        }
    }
}

data class GeminiResponse(
    val action: String,
    val parameter: String,
    val response: String,
    val language: String
)
