package com.applocker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.applocker.security.PinSecurityManager

class AppLockRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val preferences: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasPin(): Boolean = !preferences.getString(KEY_PIN_HASH, null).isNullOrBlank()

    fun setPin(pin: String) {
        preferences.edit { putString(KEY_PIN_HASH, PinSecurityManager.hashPin(pin)) }
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = preferences.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == PinSecurityManager.hashPin(pin)
    }

    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!verifyPin(currentPin)) return false
        setPin(newPin)
        return true
    }

    fun getLockedApps(): Set<String> = preferences.getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()

    fun isAppLocked(packageName: String): Boolean = getLockedApps().contains(packageName)

    fun updateLockState(packageName: String, locked: Boolean) {
        val updated = getLockedApps().toMutableSet().apply {
            if (locked) add(packageName) else remove(packageName)
        }
        preferences.edit { putStringSet(KEY_LOCKED_APPS, updated) }
    }

    companion object {
        private const val PREFS_NAME = "app_locker_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LOCKED_APPS = "locked_apps"

        @Volatile
        private var instance: AppLockRepository? = null

        fun getInstance(context: Context): AppLockRepository {
            return instance ?: synchronized(this) {
                instance ?: AppLockRepository(context).also { instance = it }
            }
        }
    }
}
