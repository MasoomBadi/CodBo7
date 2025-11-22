package com.phoenix.companionforcodblackops7.core.ads

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.phoenix.companionforcodblackops7.BuildConfig
import timber.log.Timber

@Composable
fun BannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adUnitId = if (BuildConfig.DEBUG) {
        AdMobConfig.TEST_BANNER_AD_UNIT_ID
    } else {
        AdMobConfig.BANNER_AD_UNIT_ID
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                createAdView(ctx, adUnitId)
            }
        )
    }
}

private fun createAdView(context: Context, adUnitId: String): AdView {
    return AdView(context).apply {
        setAdSize(AdSize.BANNER)
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
