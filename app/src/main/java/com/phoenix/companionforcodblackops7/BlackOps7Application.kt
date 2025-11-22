package com.phoenix.companionforcodblackops7

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BlackOps7Application : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Configure Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            // Disable crash collection in debug builds
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
