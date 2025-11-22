package com.phoenix.companionforcodblackops7.core.analytics

interface CrashlyticsHelper {
    fun logException(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
    fun recordNonFatalError(throwable: Throwable, message: String? = null)
}
