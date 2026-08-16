package com.applocker.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveAppPolicyTest {

    @Test
    fun excludesSbiBankingPackages() {
        assertTrue(SensitiveAppPolicy.shouldExclude("com.sbi.lotusintouch"))
        assertTrue(SensitiveAppPolicy.shouldExclude("com.sbi.SBIFreedomPlus"))
    }

    @Test
    fun excludesCommonFinancialPackageNames() {
        assertTrue(SensitiveAppPolicy.shouldExclude("com.example.mobilebanking"))
        assertTrue(SensitiveAppPolicy.shouldExclude("com.example.upi.payments"))
        assertTrue(SensitiveAppPolicy.shouldExclude("com.example.wallet"))
    }

    @Test
    fun allowsRegularApps() {
        assertFalse(SensitiveAppPolicy.shouldExclude("com.example.notes"))
        assertFalse(SensitiveAppPolicy.shouldExclude("com.example.gallery"))
    }
}
