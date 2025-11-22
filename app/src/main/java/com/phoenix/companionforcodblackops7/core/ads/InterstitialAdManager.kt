package com.phoenix.companionforcodblackops7.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.phoenix.companionforcodblackops7.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager @Inject constructor() {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShowTime: Long = 0

    // Minimum interval between ads (60 seconds for production, 10 seconds for debug)
    private val minIntervalMs = if (BuildConfig.DEBUG) 10_000L else 60_000L

    // Counter for actions - show ad every N actions (1 for debug testing, 3 for production)
    private var actionCounter = 0
    private val actionsBeforeAd = if (BuildConfig.DEBUG) 1 else 3

    private val adUnitId: String
        get() = if (BuildConfig.DEBUG) {
            AdMobConfig.TEST_INTERSTITIAL_AD_UNIT_ID
        } else {
            AdMobConfig.INTERSTITIAL_AD_UNIT_ID
        }

    /**
     * Load an interstitial ad. Call this early (e.g., on app start or screen load)
     */
    fun loadAd(context: Context) {
        Timber.d("Interstitial: loadAd called, isLoading=$isLoading, adReady=${interstitialAd != null}")
        if (isLoading || interstitialAd != null) {
            Timber.d("Interstitial: Skipping load - already loaded or loading")
            return
        }

        Timber.d("Interstitial: Starting to load ad with unitId=$adUnitId")
        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.d("Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    setupFullScreenCallback()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.e("Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    private fun setupFullScreenCallback() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Interstitial ad dismissed")
                interstitialAd = null
                // Don't reload immediately - will reload on next loadAd call
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Interstitial ad showed")
                lastShowTime = System.currentTimeMillis()
                actionCounter = 0
            }
        }
    }

    /**
     * Increment action counter (call when user views a detail screen)
     */
    fun recordAction() {
        actionCounter++
        Timber.d("Interstitial: recordAction called, actionCounter=$actionCounter, need=$actionsBeforeAd, adReady=${interstitialAd != null}")
    }

    /**
     * Check if enough time has passed and enough actions performed to show ad
     */
    fun canShowAd(): Boolean {
        val timeSinceLastShow = System.currentTimeMillis() - lastShowTime
        val timeOk = timeSinceLastShow >= minIntervalMs
        val actionsOk = actionCounter >= actionsBeforeAd
        val adReady = interstitialAd != null

        Timber.d("canShowAd: timeOk=$timeOk, actionsOk=$actionsOk, adReady=$adReady, actions=$actionCounter")
        return timeOk && actionsOk && adReady
    }

    /**
     * Show the interstitial ad if conditions are met
     * @param activity The activity context
     * @param onAdDismissed Callback when ad is dismissed (use for navigation)
     * @return true if ad was shown, false otherwise
     */
    fun showAdIfReady(
        activity: Activity,
        onAdDismissed: () -> Unit = {}
    ): Boolean {
        if (!canShowAd()) {
            onAdDismissed()
            return false
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Interstitial ad dismissed")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Interstitial ad showed")
                lastShowTime = System.currentTimeMillis()
                actionCounter = 0
            }
        }

        interstitialAd?.show(activity)
        return true
    }

    /**
     * Force show ad regardless of counter (still respects time interval)
     */
    fun forceShowAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {}
    ): Boolean {
        val timeSinceLastShow = System.currentTimeMillis() - lastShowTime
        if (timeSinceLastShow < minIntervalMs || interstitialAd == null) {
            onAdDismissed()
            return false
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                lastShowTime = System.currentTimeMillis()
                actionCounter = 0
            }
        }

        interstitialAd?.show(activity)
        return true
    }

    /**
     * Check if ad is loaded and ready
     */
    fun isAdReady(): Boolean = interstitialAd != null
}
