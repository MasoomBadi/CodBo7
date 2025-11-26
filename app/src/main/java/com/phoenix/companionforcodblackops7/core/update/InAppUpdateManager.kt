package com.phoenix.companionforcodblackops7.core.update

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdateManager @Inject constructor() : DefaultLifecycleObserver {

    private var appUpdateManager: AppUpdateManager? = null
    private var activityRef: WeakReference<Activity>? = null

    // Explicitly typed listener (fixes recursive type issue)
    private val installStateUpdatedListener: InstallStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {

                InstallStatus.DOWNLOADING -> {
                    val downloaded = state.bytesDownloaded()
                    val total = state.totalBytesToDownload()

                    if (total > 0) {
                        val progress = (downloaded * 100 / total).toInt()
                        Timber.d("InAppUpdate: Downloading… $progress%")
                    } else {
                        Timber.d("InAppUpdate: Downloading… calculating")
                    }
                }

                InstallStatus.INSTALLED -> {
                    Timber.d("InAppUpdate: Update installed successfully")
                    appUpdateManager?.unregisterListener(installStateUpdatedListener)
                }

                InstallStatus.FAILED -> {
                    Timber.e("InAppUpdate: Update failed")
                    appUpdateManager?.unregisterListener(installStateUpdatedListener)
                }

                else -> Timber.d("InAppUpdate: Status = ${state.installStatus()}")
            }
        }

    fun initialize(activity: Activity) {
        activityRef = WeakReference(activity)
        appUpdateManager = AppUpdateManagerFactory.create(activity)

        // Avoid duplicate listeners
        appUpdateManager?.unregisterListener(installStateUpdatedListener)
        appUpdateManager?.registerListener(installStateUpdatedListener)

        Timber.d("InAppUpdate: Manager initialized")
    }

    /**
     * Force IMMEDIATE update every time
     */
    fun checkForForcedUpdate() {
        appUpdateManager?.appUpdateInfo
            ?.addOnSuccessListener { info ->
                when (info.updateAvailability()) {

                    UpdateAvailability.UPDATE_AVAILABLE,
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {

                        val activity = activityRef?.get() ?: return@addOnSuccessListener
                        startImmediateUpdate(info, activity)
                    }

                    else -> Timber.d("InAppUpdate: App already updated")
                }
            }
            ?.addOnFailureListener { e ->
                Timber.e(e, "InAppUpdate: Failed to check updates")
            }
    }

    private fun startImmediateUpdate(info: AppUpdateInfo, activity: Activity) {
        if (!info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            Timber.e("InAppUpdate: Immediate update not allowed")
            return
        }

        try {
            Timber.d("InAppUpdate: Starting mandatory (immediate) update")
            appUpdateManager?.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                REQUEST_CODE_IMMEDIATE_UPDATE
            )
        } catch (e: Exception) {
            Timber.e(e, "InAppUpdate: Failed to start immediate update")
        }
    }

    /**
     * Resume update if user minimized app
     */
    fun resumeIfNeeded(activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
            if (info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                Timber.d("InAppUpdate: Resuming mandatory update")
                startImmediateUpdate(info, activity)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager?.unregisterListener(installStateUpdatedListener)
        activityRef?.clear()
        Timber.d("InAppUpdate: Manager destroyed")
    }

    companion object {
        const val REQUEST_CODE_IMMEDIATE_UPDATE = 2002
    }
}
