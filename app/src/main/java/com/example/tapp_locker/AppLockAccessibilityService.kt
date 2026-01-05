package com.example.tapp_locker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AppLockAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TODO: Implement your app lock logic here
    }

    override fun onInterrupt() {
        // TODO: Handle interruptions
    }
}