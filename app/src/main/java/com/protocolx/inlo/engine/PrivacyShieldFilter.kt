package com.protocolx.inlo.engine

object PrivacyShieldFilter {
    private val SENSITIVE_PACKAGE_KEYWORDS = listOf(
        "bank", "banking", "authenticator", "otp", "pass", "vault", "wallet", "paytm", "gpay", "phonepe"
    )

    private val OTP_PATTERNS = listOf(
        Regex("(?i)\\b(\\d{4,8})\\b.*(is your (otp|verification|security code|pin))"),
        Regex("(?i)(otp|verification code|security code|passcode)[:\\s]+(\\d{4,8})"),
        Regex("(?i)\\bdo not share.*(code|otp)"),
        Regex("(?i)(password reset|temporary password)")
    )

    fun isSensitive(packageName: String, title: String, text: String): Boolean {
        val lowerPkg = packageName.lowercase()
        if (SENSITIVE_PACKAGE_KEYWORDS.any { lowerPkg.contains(it) }) {
            return true
        }

        val combinedText = "$title $text"
        return OTP_PATTERNS.any { it.containsMatchIn(combinedText) }
    }
}
