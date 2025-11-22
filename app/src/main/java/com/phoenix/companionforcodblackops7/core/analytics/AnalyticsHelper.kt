package com.phoenix.companionforcodblackops7.core.analytics

import android.os.Bundle

interface AnalyticsHelper {
    fun logEvent(eventName: String, params: Bundle? = null)
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun logButtonClick(buttonName: String, screenName: String? = null)
    fun logFeatureUsed(featureName: String, details: Map<String, String>? = null)
    fun setUserProperty(name: String, value: String)
}
