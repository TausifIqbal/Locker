package com.example.tapp_locker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppLockAccessibilityService : AccessibilityService() {

    companion object {
        var lastUnlockedPackage: String? = null
    }

    private val lockedPackages = setOf(
        "com.android.settings", 
        "com.google.android.calculator",
        "com.google.android.deskclock", 
        "com.google.android.gm"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d("AppLockService", "Window state changed: $packageName")

            // 1. If it's a locked app
            if (lockedPackages.contains(packageName)) {
                // Only launch if it's not the one we JUST unlocked
                if (packageName != lastUnlockedPackage) {
                    launchLockScreen(packageName)
                }
            } else {
                // 2. If user moves to a different app (that is not our locker), 
                // reset the unlock state so it locks again next time.
                if (packageName != "com.example.tapp_locker") {
                    lastUnlockedPackage = null
                }
            }
        }
    }

    private fun launchLockScreen(packageName: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            // Pass the package name so the Activity knows what it's unlocking
            putExtra("TARGET_PACKAGE", packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}