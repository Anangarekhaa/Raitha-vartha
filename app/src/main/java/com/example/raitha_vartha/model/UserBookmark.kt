package com.example.raitha_vartha.model

import androidx.room.Entity

@Entity(tableName = "user_bookmarks", primaryKeys = ["tipId", "userPhone"])
data class UserBookmark(
    val tipId: Int,
    val userPhone: String
)
