package com.example.raitha_vartha.model

data class RegionInfo(
    val locationName: String = "",
    val regionType: String = "General", // Coastal, Dry, Malnad, etc.
    val soilType: String = "Unknown",
    val weather: String = "Loading..."
)
