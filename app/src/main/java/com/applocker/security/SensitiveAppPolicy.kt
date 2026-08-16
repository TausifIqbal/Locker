package com.applocker.security

/**
 * Keeps AppLocker away from banking and payment apps.
 *
 * Many financial apps treat accessibility-based app lockers as a risk signal because they can
 * appear over the banking screen or observe foreground-window changes. To reduce false malware
 * warnings and avoid interfering with apps such as SBI YONO, AppLocker never offers these apps
 * for locking and the accessibility service ignores them even if an old preference remains.
 */
object SensitiveAppPolicy {

    private val knownFinancialPackages = setOf(
        "com.sbi.lotusintouch", // YONO SBI
        "com.sbi.SBIFreedomPlus" // YONO Lite SBI
    )

    private val financialPackageKeywords = listOf(
        "bank",
        "banking",
        "sbi",
        "yono",
        "upi",
        "bhim",
        "paytm",
        "phonepe",
        "gpay",
        "tez",
        "wallet"
    )

    fun shouldExclude(packageName: String): Boolean {
        val normalizedPackageName = packageName.lowercase()
        return packageName in knownFinancialPackages ||
            financialPackageKeywords.any { keyword -> normalizedPackageName.contains(keyword) }
    }
}
