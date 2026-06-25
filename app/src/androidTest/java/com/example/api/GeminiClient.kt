package com.example.api

import android.util.Log
import com.example.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"

    data class AnalysisResult(
        val action: String,
        val parameter: String,
        val response: String,
        val language: String
    )

    fun analyzeCommand(commandText: String): AnalysisResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "\"\"") {
            return AnalysisResult("NONE", "", "د رئیس صاحب، مهرباني وکړئ لومړی خپل API Key تنظیم کړئ. (کلید API تنظیم نشده است)", "ps")
        }

        try {
            val url = URL("$API_URL?key=$apiKey")
            val conn = HttpURLConnection.openConnection(url) as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // پرامپت سیستم برای پاسخ‌دهی دقیق و تفکیک زبانی
            val systemInstruction = "You are 'د رئیس ږغ RAEESTALK' voice assistant. Detect the language from [Pashto, Dari, Farsi, English, Arabic, Urdu]. Respond flawlessly in the detected language. If the user asks for a system action (call, alarm, sms, navigate), return a strict JSON inside the response text. Format: {\"action\":\"CALL_PHONE\"|\"SET_ALARM\"|\"NAVIGATE\"|\"NONE\", \"parameter\":\"target info\", \"reply\":\"your voice response\"}"

            val jsonRequest = JSONObject().apply {
                put("contents", JSONObject().apply {
                    put("parts", JSONObject().apply {
                        put("text", "System Instruction: $systemInstruction \nUser Command: $commandText")
                    })
                })
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonRequest.toString())
            writer.flush()
            writer.close()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseStr = reader.use { it.readText() }
                
                // تحلیل پاسخ دریافتی از جمینای
                val jsonResponse = JSONObject(responseStr)
                val rawText = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // تشخیص خودکار کدهای استاندارد زبان برای موتور صوتی (TTS)
                val detectedLang = when {
                    commandText.contains(Regex("[ښځڅډټۍېۍلو]")) -> "ps" // پشتو
                    commandText.contains(Regex("[چگپژ]")) -> "fa" // فارسی / دری
                    commandText.contains(Regex("[ٹڈڑںے]")) -> "ur" // اردو
                    commandText.contains(Regex("[يةؤئإأ]")) -> "ar" // عربی
                    commandText.contains(Regex("[a-zA-Z]")) -> "en" // انگلیسی
                    else -> "fa"
                }

                return try {
                    val cleanJson = rawText.substring(rawText.indexOf("{"), rawText.lastIndexOf("}") + 1)
                    val actionObj = JSONObject(cleanJson)
                    AnalysisResult(
                        action = actionObj.optString("action", "NONE"),
                        parameter = actionObj.optString("parameter", ""),
                        response = actionObj.optString("reply", rawText),
                        language = detectedLang
                    )
                } catch (e: Exception) {
                    AnalysisResult("NONE", "", rawText, detectedLang)
                }
            } else {
                Log.e(TAG, "API Error: ${conn.responseCode}")
                return AnalysisResult("NONE", "", "Error connecting to AI Studio server", "en")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network Exception", e)
            return AnalysisResult("NONE", "", "ارتباط برقرار نشد. لطفاً اینترنت خود را چک کنید.", "fa")
        }
    }
}
