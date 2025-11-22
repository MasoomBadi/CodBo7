package com.phoenix.companionforcodblackops7.core.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.phoenix.companionforcodblackops7.BuildConfig
import timber.log.Timber

/**
 * Set to true when taking screenshots for Play Store.
 * Remember to set back to false before release!
 */
private const val SCREENSHOT_MODE = false

/**
 * Banner Ad composable that displays an adaptive banner ad at the bottom of screens.
 * Uses anchored adaptive banner which adjusts to screen width for optimal fill rate.
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier
) {
    // Hide ads in screenshot mode
    if (SCREENSHOT_MODE) return

    val context = LocalContext.current
    val adUnitId = if (BuildConfig.DEBUG) {
        AdMobConfig.TEST_BANNER_AD_UNIT_ID
    } else {
        AdMobConfig.BANNER_AD_UNIT_ID
    }

    // Get adaptive banner size based on screen width
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val density = LocalDensity.current

    val adSize = remember(screenWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    // Calculate height based on adaptive ad size
    val adHeightDp = with(density) { adSize.height.dp }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                createAdView(ctx, adUnitId, adSize)
            }
        )
    }
}

private fun createAdView(context: Context, adUnitId: String, adSize: AdSize): AdView {
    return AdView(context).apply {
        setAdSize(adSize)
        this.adUnitId = adUnitId

        adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.d("Banner ad loaded successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Timber.e("Banner ad failed to load: ${error.message}")
            }

            override fun onAdClicked() {
                Timber.d("Banner ad clicked")
            }

            override fun onAdImpression() {
                Timber.d("Banner ad impression recorded")
            }
        }

        loadAd(AdRequest.Builder().build())
    }
}
