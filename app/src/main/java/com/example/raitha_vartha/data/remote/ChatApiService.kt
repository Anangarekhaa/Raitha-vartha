package com.example.raitha_vartha.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.raitha_vartha.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatApiService(private val context: Context) {
    // API Key is now safely pulled from local.properties via BuildConfig
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
    private val gson = Gson()

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getAgriAdvice(userInput: String, language: String): String = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext if (language == "kn") "ತಜ್ಞರ ಸಲಹೆಗಾಗಿ ಇಂಟರ್ನೆಟ್ ಅಗತ್ಯವಿದೆ." 
                               else "Internet required for expert advice."
        }

        try {
            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-goog-api-key", apiKey)
            connection.doOutput = true

            val langInstruction = if (language == "kn") "Respond strictly in Kannada language." else "Respond in English."

            val prompt = """
                You are Mitra AI, an agriculture expert helping farmers in Karnataka.
                $langInstruction
                Keep your response SHORT and concise .
                Give a practical, easy-to-understand answer.
                Focus only on actionable advice for the farmer.
                IMPORTANT: Use plain text only. Do NOT use any markdown formatting like asterisks (**), hashtags (##), or lists.
                Question: $userInput
            """.trimIndent()

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )

            OutputStreamWriter(connection.outputStream).use { it.write(gson.toJson(requestBody)) }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val response = gson.fromJson(responseText, GeminiResponse::class.java)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: (if (language == "kn") "ನಮಗೆ ಉತ್ತರ ಸಿಗಲಿಲ್ಲ." else "I couldn't find an answer.")
                
                // Final thorough cleaning of markdown and special characters
                rawText.replace(Regex("[#*]"), "").trim()
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e("ChatApiService", "API Error $responseCode: $errorText")
                if (language == "kn") "ಕ್ಷಮಿಸಿ, ಸಂಪರ್ಕ ದೋಷ ಸಂಭವಿಸಿದೆ. ದಯವಿಟ್ಟು ನಂತರ ಪ್ರಯತ್ನಿಸಿ."
                else "Sorry, Mitra is currently having trouble connecting. Please try again later."
            }
        } catch (e: Exception) {
            Log.e("ChatApiService", "Exception: ${e.message}", e)
            if (language == "kn") "ಸಂಪರ್ಕ ದೋಷ ಸಂಭವಿಸಿದೆ."
            else "Connection error. Please check your internet and try again."
        }
    }

    suspend fun getRawGeminiResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-goog-api-key", apiKey)
            connection.doOutput = true

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )

            OutputStreamWriter(connection.outputStream).use { it.write(gson.toJson(requestBody)) }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val response = gson.fromJson(responseText, GeminiResponse::class.java)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private data class GeminiResponse(val candidates: List<Candidate>?)
    private data class Candidate(val content: Content?)
    private data class Content(val parts: List<Part>?)
    private data class Part(val text: String?)
}
