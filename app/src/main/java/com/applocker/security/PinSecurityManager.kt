package com.applocker.security

import java.security.MessageDigest

object PinSecurityManager {

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return buildString(hashBytes.size * 2) {
            hashBytes.forEach { append("%02x".format(it)) }
        }
    }

    fun isValidPinFormat(pin: String): Boolean = pin.length == 4 && pin.all(Char::isDigit)
}
