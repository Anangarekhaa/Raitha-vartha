package com.example.raitha_vartha.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_vartha.data.local.AppDatabase
import com.example.raitha_vartha.data.remote.ChatApiService
import com.example.raitha_vartha.data.repository.TipRepository
import com.example.raitha_vartha.model.Tip
import com.example.raitha_vartha.model.RegionInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class TipViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TipRepository
    private val tipDao = AppDatabase.getDatabase(application).tipDao()
    private val chatApiService = ChatApiService(application)
    private val sharedPrefs = application.getSharedPreferences("raitha_prefs", Context.MODE_PRIVATE)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _selectedCrop = MutableStateFlow<String?>(null)
    val selectedCrop: StateFlow<String?> = _selectedCrop.asStateFlow()

    private val _userPhone = MutableStateFlow<String?>(null)
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _onlySuccessStories = MutableStateFlow(false)
    val onlySuccessStories: StateFlow<Boolean> = _onlySuccessStories.asStateFlow()

    private val _regionInfo = MutableStateFlow(RegionInfo(locationName = "Detecting..."))
    val regionInfo: StateFlow<RegionInfo> = _regionInfo.asStateFlow()

    private val _hasShownWelcomeTip = MutableStateFlow(false)

    // Chat states
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    val tips: StateFlow<List<Tip>> = combine(
        _selectedCrop, 
        _currentLanguage, 
        _userPhone,
        _onlySuccessStories,
        _regionInfo
    ) { crop, lang, phone, onlySuccess, region ->
        repository.getTips(crop, lang, phone, onlySuccess).map { allTips ->
            if (allTips.isEmpty()) return@map emptyList<Tip>()
            
            // 1. Language Filter
            val langTips = allTips.filter { it.language == lang }
            
            // 2. Separate Welcome from Content
            val welcomeTip = langTips.find { it.cropType.lowercase() == "general" }
            val otherTips = langTips.filter { it.cropType.lowercase() != "general" }

            // 3. Crop Filtering
            val filteredOther = if (crop != null && crop != "All") {
                otherTips.filter { it.cropType.equals(crop, ignoreCase = true) }
            } else {
                otherTips
            }
            
            // 4. Region Prioritization (Stable Sorting)
            val sortedOther = if (crop == null || crop == "All") {
                val regionKeywords = listOf(region.regionType, region.locationName).filter { it.isNotBlank() && it != "General" && it != "Detecting..." }
                val regionSpecific = filteredOther.filter { tip ->
                    regionKeywords.any { keyword -> 
                        tip.description.contains(keyword, ignoreCase = true) || 
                        tip.title.contains(keyword, ignoreCase = true)
                    }
                }
                // Mix region specific at start, but keep variety
                (regionSpecific.shuffled() + (filteredOther - regionSpecific.toSet()).shuffled())
            } else {
                filteredOther.shuffled()
            }

            // 5. Build Final List
            val finalResult = mutableListOf<Tip>()
            // Always show welcome tip first for "All" view if we have it
            if (welcomeTip != null && (crop == null || crop == "All") && !onlySuccess) {
                finalResult.add(welcomeTip)
            }
            finalResult.addAll(sortedOther)
            
            Log.d("TipViewModel", "Returning ${finalResult.size} tips for lang=$lang crop=$crop")
            finalResult.toList()
        }
    }.flatMapLatest { it }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkSession()
        repository = TipRepository(tipDao, application)
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            // Try to load saved location if available, else fetch
            val savedLocation = sharedPrefs.getString("last_location_name", null)
            if (savedLocation != null) {
                updateRegionByLocation(savedLocation)
            } else {
                fetchCurrentLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                if (!isLocationEnabled()) {
                    _regionInfo.value = _regionInfo.value.copy(locationName = "GPS Off", weather = "Please enable location")
                    return@launch
                }

                _regionInfo.value = _regionInfo.value.copy(weather = "GPS Linking...")
                val cts = CancellationTokenSource()
                val location: Location? = try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                        ?: fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    Log.e("TipViewModel", "fusedLocationClient error", e)
                    null
                }

                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality 
                        ?: addresses?.firstOrNull()?.subAdminArea 
                        ?: addresses?.firstOrNull()?.adminArea 
                        ?: "Unknown Location"
                    updateRegionByLocation(cityName)
                } else {
                    _regionInfo.value = _regionInfo.value.copy(locationName = "Signal Weak", weather = "Tap to set manually")
                }
            } catch (e: Exception) {
                Log.e("TipViewModel", "Location error", e)
                _regionInfo.value = _regionInfo.value.copy(locationName = "Error", weather = "Tap to set manually")
            }
        }
    }

    fun updateRegionByLocation(locationName: String) {
        viewModelScope.launch {
            _regionInfo.value = _regionInfo.value.copy(locationName = locationName, weather = "Syncing...")
            
            // Save last successful location
            sharedPrefs.edit().putString("last_location_name", locationName).apply()

            val prompt = """
                Based on the location "$locationName" in Karnataka, India, identify:
                1. Region Type (Coastal, Dry, Malnad, or North Karnataka)
                2. Primary Soil Type
                3. Typical Current Weather prediction for this season.
                Respond strictly in JSON format:
                {"regionType": "...", "soilType": "...", "weather": "..."}
            """.trimIndent()

            val response = chatApiService.getRawGeminiResponse(prompt)
            response?.let {
                try {
                    val cleanJson = it.replace("```json", "").replace("```", "").trim()
                    val info = com.google.gson.Gson().fromJson(cleanJson, RegionInfo::class.java)
                    _regionInfo.value = info.copy(locationName = locationName)
                } catch (e: Exception) {
                    Log.e("TipViewModel", "Error parsing region JSON", e)
                    _regionInfo.value = _regionInfo.value.copy(weather = "Info unavailable")
                }
            }
        }
    }

    private fun checkSession() {
        val phone = sharedPrefs.getString("user_phone", null)
        val name = sharedPrefs.getString("user_name", null)
        val loginTime = sharedPrefs.getLong("login_timestamp", 0L)
        val now = System.currentTimeMillis()

        if (phone != null && (now - loginTime) < 3600000) {
            _userPhone.value = phone
            _userName.value = name
        } else {
            logout()
        }
    }

    fun logout() {
        _userPhone.value = null
        _userName.value = null
        sharedPrefs.edit()
            .remove("user_phone")
            .remove("user_name")
            .remove("login_timestamp")
            .apply()
    }

    fun markWelcomeShown() {
        _hasShownWelcomeTip.value = true
    }

    fun askChatbot(question: String) {
        viewModelScope.launch {
            val contextInfo = "User Location: ${_regionInfo.value.locationName}, Region: ${_regionInfo.value.regionType}, Soil: ${_regionInfo.value.soilType}."
            val enhancedQuestion = "$contextInfo\nQuestion: $question"
            
            val userMsg = ChatMessage(question, true)
            _chatMessages.value = _chatMessages.value + userMsg
            
            _isChatLoading.value = true
            val result = chatApiService.getAgriAdvice(enhancedQuestion, _currentLanguage.value)
            
            val botMsg = ChatMessage(result, false)
            _chatMessages.value = _chatMessages.value + botMsg
            
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun setLanguage(language: String) {
        _currentLanguage.value = language
    }

    fun setCropFilter(crop: String?) {
        _selectedCrop.value = if (crop == "All" || crop == null) null else crop
    }

    fun toggleSuccessStories(onlySuccess: Boolean) {
        _onlySuccessStories.value = onlySuccess
    }

    fun login(phone: String, name: String) {
        _userPhone.value = phone
        _userName.value = name
        sharedPrefs.edit()
            .putString("user_phone", phone)
            .putString("user_name", name)
            .putLong("login_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun toggleBookmark(tip: Tip) {
        viewModelScope.launch {
            _userPhone.value?.let { phone ->
                repository.toggleBookmark(tip.id, phone)
            }
        }
    }
}
