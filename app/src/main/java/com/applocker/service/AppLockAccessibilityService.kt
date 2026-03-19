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

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppLockRepository.getInstance(this)
        launcherPackages = AppUtils.getLauncherPackages(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        LockSessionManager.clearAllExpired()

        val packageName = event.packageName?.toString().orEmpty()
        if (packageName.isBlank()) return
        if (packageName == packageNameOfAppLocker()) return
        if (packageName in launcherPackages) return
        if (!repository.isAppLocked(packageName)) {
            LockSessionManager.clearUnlock(packageName)
            return
        }
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

    private fun packageNameOfAppLocker(): String = packageName

    companion object {
        fun serviceId(contextPackage: String): String {
            return ComponentName(contextPackage, AppLockAccessibilityService::class.java.name).flattenToString()
        }
    }
}
