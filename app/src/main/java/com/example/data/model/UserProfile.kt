package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val provider: String // "google" or "microsoft"
)
