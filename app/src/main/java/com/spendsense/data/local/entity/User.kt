package com.spendsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Only 1 user needed for offline mode
    val name: String,
    val email: String,
    val currency: String
)
