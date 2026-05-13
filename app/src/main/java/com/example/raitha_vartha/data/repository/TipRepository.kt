package com.example.raitha_vartha.data.repository

import android.content.Context
import android.util.Log
import com.example.raitha_vartha.data.local.TipDao
import com.example.raitha_vartha.data.remote.ChatApiService
import com.example.raitha_vartha.model.Tip
import com.example.raitha_vartha.model.UserBookmark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.Calendar

class TipRepository(
    private val tipDao: TipDao,
    private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("raitha_prefs", Context.MODE_PRIVATE)
    private val chatApiService = ChatApiService(context)
    private val gson = Gson()

    fun getTips(cropType: String?, language: String, userPhone: String?, onlySuccessStories: Boolean = false): Flow<List<Tip>> {
        val filter = if (cropType == null || cropType == "All") null else cropType.lowercase()
        return tipDao.getTipsWithBookmarks(filter, language, userPhone, if (onlySuccessStories) 1 else 0)
    }

    suspend fun toggleBookmark(tipId: Int, userPhone: String) {
        withContext(Dispatchers.IO) {
            val isBookmarked = tipDao.isBookmarked(tipId, userPhone)
            if (isBookmarked) {
                tipDao.deleteBookmark(UserBookmark(tipId, userPhone))
            } else {
                tipDao.insertBookmark(UserBookmark(tipId, userPhone))
            }
        }
    }

    suspend fun initializeDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            try {
                if (tipDao.getCount() == 0) {
                    val tips = loadTipsFromJson()
                    if (tips.isNotEmpty()) {
                        tipDao.insertTips(tips)
                        Log.d("TipRepository", "Database initialized with ${tips.size} tips from JSON")
                    }
                }
                syncAiTips()
            } catch (e: Exception) {
                Log.e("TipRepository", "Error initializing database", e)
            }
        }
    }

    private suspend fun syncAiTips() {
        if (!chatApiService.isOnline()) return

        val lastSync = sharedPrefs.getLong("last_ai_sync", 0L)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (lastSync >= today) return

        try {
            val prompt = """
                You are an agriculture expert.
                Generate 3–5 short, practical farming tips.
                Requirements:
                - Each tip must be 1–2 sentences
                - Must be actionable (clear instruction)
                - Must be crop-specific (paddy, coconut, tomato, areca nut)
                - Avoid technical jargon
                - Keep language simple and clear
                - Provide response strictly in JSON format as a list of objects with fields:
                  "title_en", "desc_en", "title_kn", "desc_kn", "cropType" (paddy, coconut, tomato, or areca nut)
            """.trimIndent()

            val response = chatApiService.getRawGeminiResponse(prompt)
            if (response != null) {
                val cleanResponse = response.replace("```json", "").replace("```", "").trim()
                val type = object : TypeToken<List<AiTip>>() {}.type
                val aiTips: List<AiTip> = gson.fromJson(cleanResponse, type)

                var maxId = tipDao.getMaxId() ?: 0
                val newTips = mutableListOf<Tip>()

                aiTips.forEach { aiTip ->
                    val timestamp = System.currentTimeMillis()
                    val image = when (aiTip.cropType.lowercase()) {
                        "paddy" -> "paddy"
                        "coconut" -> "coconut"
                        "tomato" -> "tomato"
                        "areca nut" -> "areca_nut"
                        else -> "farm"
                    }

                    maxId++
                    newTips.add(Tip(
                        id = maxId,
                        title = aiTip.title_en,
                        description = aiTip.desc_en,
                        cropType = aiTip.cropType.lowercase(),
                        language = "en",
                        image = image,
                        timestamp = timestamp,
                        fromInternet = true
                    ))

                    maxId++
                    newTips.add(Tip(
                        id = maxId,
                        title = aiTip.title_kn,
                        description = aiTip.desc_kn,
                        cropType = aiTip.cropType.lowercase(),
                        language = "kn",
                        image = image,
                        timestamp = timestamp,
                        fromInternet = true
                    ))
                }

                if (newTips.isNotEmpty()) {
                    tipDao.insertTips(newTips)
                    sharedPrefs.edit().putLong("last_ai_sync", System.currentTimeMillis()).apply()
                    Log.d("TipRepository", "Added ${newTips.size / 2} new AI tips")
                }
            }
        } catch (e: Exception) {
            Log.e("TipRepository", "Error syncing AI tips", e)
        }
    }

    private fun loadTipsFromJson(): List<Tip> {
        return try {
            val inputStream = context.assets.open("tips.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Tip>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            Log.e("TipRepository", "Error loading JSON", e)
            emptyList()
        }
    }

    private data class AiTip(
        val title_en: String,
        val desc_en: String,
        val title_kn: String,
        val desc_kn: String,
        val cropType: String
    )
}
