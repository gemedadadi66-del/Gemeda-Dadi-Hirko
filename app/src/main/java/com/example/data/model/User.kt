package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val organization: String,
    val roleTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)
