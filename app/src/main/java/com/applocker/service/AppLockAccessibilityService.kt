package com.applocker.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applocker.data.AppLockRepository
import com.applocker.ui.LockScreenActivity
import com.applocker.utils.AppUtils
import com.applocker.utils.LockSessionManager

class AppLockAccessibilityService : AccessibilityService() {

    private lateinit var repository: AppLockRepository
    private lateinit var launcherPackages: Set<String>
    
    // IN-MEMORY CACHE for battery optimization
    private val lockedPackagesCache = HashSet<String>()
    private var lastCacheUpdateTime = 0L
    private val CACHE_REFRESH_INTERVAL = 30000L // 30 seconds

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppLockRepository.getInstance(this)
        launcherPackages = AppUtils.getLauncherPackages(this)
        refreshCache()
    }

    private fun refreshCache() {
        lockedPackagesCache.clear()
        lockedPackagesCache.addAll(repository.getLockedApps())
        lastCacheUpdateTime = System.currentTimeMillis()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // LEAN LOGIC: Filter events as early as possible
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isBlank()) return

        // Skip self and launcher (High-frequency windows)
        if (packageName == this.packageName) return
        if (packageName in launcherPackages) return

        // Throttle cache refresh to avoid frequent Disk I/O
        if (System.currentTimeMillis() - lastCacheUpdateTime > CACHE_REFRESH_INTERVAL) {
            refreshCache()
        }

        // LEAN CHECK: O(1) lookup in memory instead of reading from SharedPreferences
        if (!lockedPackagesCache.contains(packageName)) {
            LockSessionManager.clearUnlock(packageName)
            return
        }

        // Efficient session handling
        if (!LockSessionManager.shouldPrompt(packageName)) return

        LockSessionManager.notifyPromptShown(packageName)
        startActivity(
            Intent(this, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, packageName)
            }
        )
    }

    override fun onInterrupt() = Unit

    companion object {
        fun serviceId(contextPackage: String): String {
            return ComponentName(contextPackage, AppLockAccessibilityService::class.java.name).flattenToString()
        }
    }
}
