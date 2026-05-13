package com.example.raitha_vartha.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val name: String
)
