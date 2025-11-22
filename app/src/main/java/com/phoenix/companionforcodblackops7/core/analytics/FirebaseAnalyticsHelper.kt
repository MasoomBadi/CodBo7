package com.phoenix.companionforcodblackops7.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsHelper {

    override fun logEvent(eventName: String, params: Bundle?) {
        firebaseAnalytics.logEvent(eventName, params)
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
    }

    override fun logButtonClick(buttonName: String, screenName: String?) {
        firebaseAnalytics.logEvent("button_click") {
            param("button_name", buttonName)
            screenName?.let { param("screen_name", it) }
        }
    }

    override fun logFeatureUsed(featureName: String, details: Map<String, String>?) {
        firebaseAnalytics.logEvent("feature_used") {
            param("feature_name", featureName)
            details?.forEach { (key, value) ->
                param(key, value)
            }
        }
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
