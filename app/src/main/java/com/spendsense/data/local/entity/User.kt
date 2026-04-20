package com.spendsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val profilePic: String = "",
    val isGuest: Boolean = false,
    val currency: String = "INR",
    val incomeRange: String = "0-25k",
    val personality: String = "Balanced",
    val healthScore: Int = 70
)
