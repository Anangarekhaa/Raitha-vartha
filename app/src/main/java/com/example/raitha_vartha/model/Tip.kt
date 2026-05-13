package com.example.raitha_vartha.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class Tip(
    @PrimaryKey val id: Int,

    val title: String,
    val description: String,
    val cropType: String,
    val language: String,
    val image: String,

    val timestamp: Long,

    val category: String? = null,
    val isBookmarked: Boolean = false,
    val userPhone: String? = null,
    val isSuccessStory: Boolean = false,
    val fromInternet: Boolean = false
)