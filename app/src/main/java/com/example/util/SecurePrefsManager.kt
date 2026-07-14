package com.example.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object SecurePrefsManager {
    private const val PREFS_FILE = "secure_oauth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_PROFILE = "user_profile"
    private const val KEY_DARK_MODE = "dark_mode_setting" // "system", "light", "dark"

    private val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    private val profileAdapter by lazy {
        moshi.adapter(UserProfile::class.java)
    }

    private fun getSharedPrefs(context: Context): android.content.SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                // Delete corrupt preferences and try recreating
                context.deleteSharedPreferences(PREFS_FILE)
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
                // Bulletproof fallback to standard unencrypted preferences if KeyStore is fully inaccessible
                context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveToken(context: Context, token: String) {
        getSharedPrefs(context).edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getSharedPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveUserProfile(context: Context, profile: UserProfile) {
        val json = profileAdapter.toJson(profile)
        getSharedPrefs(context).edit().putString(KEY_USER_PROFILE, json).apply()
    }

    fun getUserProfile(context: Context): UserProfile? {
        val json = getSharedPrefs(context).getString(KEY_USER_PROFILE, null) ?: return null
        return try {
            profileAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun clearAuth(context: Context) {
        getSharedPrefs(context).edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_PROFILE)
            .apply()
    }

    fun saveDarkMode(context: Context, mode: String) {
        getSharedPrefs(context).edit().putString(KEY_DARK_MODE, mode).apply()
    }

    fun getDarkMode(context: Context): String {
        return getSharedPrefs(context).getString(KEY_DARK_MODE, "system") ?: "system"
    }
}
