package com.applocker.utils

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
        temporarilyUnlockedApps[packageName] = System.currentTimeMillis() + UNLOCK_COOLDOWN_MS
        lastPromptedPackage = packageName
        lastPromptTimestamp = System.currentTimeMillis()
        isLockScreenVisible = false
    }

    @Synchronized
    fun shouldPrompt(packageName: String): Boolean {
        val now = System.currentTimeMillis()
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
        temporarilyUnlockedApps.remove(packageName)
    }

    @Synchronized
    fun clearAllExpired() {
        val now = System.currentTimeMillis()
        temporarilyUnlockedApps.entries.removeAll { it.value <= now }
    }

    @Synchronized
    fun notifyPromptShown(packageName: String) {
        lastPromptedPackage = packageName
        lastPromptTimestamp = System.currentTimeMillis()
        isLockScreenVisible = true
    }
}
