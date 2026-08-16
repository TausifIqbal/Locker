package com.applocker.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import com.applocker.data.AppLockRepository
import com.applocker.ui.LockScreenActivity
import com.applocker.utils.AppUtils
import com.applocker.utils.LockSessionManager

class AppLockAccessibilityService : AccessibilityService() {

    private lateinit var repository: AppLockRepository
    private lateinit var launcherPackages: Set<String>
    private var lockedAppsPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    // IN-MEMORY CACHE for battery optimization
    private val lockedPackagesCache = HashSet<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppLockRepository.getInstance(this)
        launcherPackages = AppUtils.getLauncherPackages(this)
        refreshCache()
        lockedAppsPreferenceListener = repository.registerLockedAppsChangeListener {
            refreshCache()
        }
    }

    @Synchronized
    private fun refreshCache() {
        lockedPackagesCache.clear()
        lockedPackagesCache.addAll(repository.getLockedApps())
    }

    @Synchronized
    private fun isLockedPackage(packageName: String): Boolean {
        return lockedPackagesCache.contains(packageName)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // LEAN LOGIC: Filter events as early as possible
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isBlank()) return

        // Skip self and launcher (High-frequency windows)
        if (packageName == this.packageName) return
        if (packageName in launcherPackages) return

        // LEAN CHECK: O(1) lookup in memory instead of reading from SharedPreferences
        if (!isLockedPackage(packageName)) {
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

    override fun onDestroy() {
        lockedAppsPreferenceListener?.let(repository::unregisterLockedAppsChangeListener)
        lockedAppsPreferenceListener = null
        super.onDestroy()
    }

    companion object {
        fun serviceId(contextPackage: String): String {
            return ComponentName(contextPackage, AppLockAccessibilityService::class.java.name).flattenToString()
        }
    }
}
