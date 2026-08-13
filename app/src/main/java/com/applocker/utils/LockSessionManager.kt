package com.applocker.utils

import android.os.SystemClock

object LockSessionManager {
    private const val UNLOCK_COOLDOWN_MS = 30_000L

    @Volatile
    var isLockScreenVisible: Boolean = false

    @Volatile
    private var lastPromptedPackage: String? = null

    @Volatile
    private var lastPromptTimestamp: Long = 0L

    private val temporarilyUnlockedApps = mutableMapOf<String, Long>()

    @Synchronized
    fun markAppUnlocked(packageName: String) {
        val now = SystemClock.elapsedRealtime()
        temporarilyUnlockedApps[packageName] = now + UNLOCK_COOLDOWN_MS
        lastPromptedPackage = packageName
        lastPromptTimestamp = now
        isLockScreenVisible = false
    }

    @Synchronized
    fun shouldPrompt(packageName: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        val unlockExpiry = temporarilyUnlockedApps[packageName]
        
        if (unlockExpiry != null && unlockExpiry > now) {
            return false
        }
        
        if (lastPromptedPackage == packageName && now - lastPromptTimestamp < 1_000L) {
            return false
        }
        
        if (unlockExpiry != null && unlockExpiry <= now) {
            temporarilyUnlockedApps.remove(packageName)
        }
        
        return !isLockScreenVisible
    }

    @Synchronized
    fun clearUnlock(packageName: String) {
        // Optimization: check if the package is actually there to avoid unnecessary map mutations
        if (temporarilyUnlockedApps.containsKey(packageName)) {
            temporarilyUnlockedApps.remove(packageName)
        }
    }

    @Synchronized
    fun clearAllExpired() {
        if (temporarilyUnlockedApps.isEmpty()) return
        
        val now = SystemClock.elapsedRealtime()
        temporarilyUnlockedApps.entries.removeAll { it.value <= now }
    }

    @Synchronized
    fun notifyPromptShown(packageName: String) {
        lastPromptedPackage = packageName
        lastPromptTimestamp = SystemClock.elapsedRealtime()
        isLockScreenVisible = true
    }
}
