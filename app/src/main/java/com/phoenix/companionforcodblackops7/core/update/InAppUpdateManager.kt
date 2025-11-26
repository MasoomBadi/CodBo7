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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdateManager @Inject constructor() : DefaultLifecycleObserver {

    private var appUpdateManager: AppUpdateManager? = null
    private var currentActivity: Activity? = null

    // Listener for update download progress
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                Timber.d("InAppUpdate: Downloading... $progress%")
            }
            InstallStatus.DOWNLOADED -> {
                Timber.d("InAppUpdate: Download completed. Notifying user to complete update.")
                // For flexible updates, notify user that update is ready to install
                onUpdateDownloaded?.invoke()
            }
            InstallStatus.INSTALLED -> {
                Timber.d("InAppUpdate: Update installed successfully")
                appUpdateManager?.unregisterListener(installStateUpdatedListener)
            }
            InstallStatus.FAILED -> {
                Timber.e("InAppUpdate: Update failed")
                appUpdateManager?.unregisterListener(installStateUpdatedListener)
            }
            else -> {
                Timber.d("InAppUpdate: Install status: ${state.installStatus()}")
            }
        }
    }

    // Callback when flexible update is downloaded
    var onUpdateDownloaded: (() -> Unit)? = null

    /**
     * Initialize the update manager and check for updates
     */
    fun initialize(activity: Activity) {
        currentActivity = activity
        appUpdateManager = AppUpdateManagerFactory.create(activity)

        // Register listener for update state changes
        appUpdateManager?.registerListener(installStateUpdatedListener)

        Timber.d("InAppUpdate: Manager initialized")
    }

    /**
     * Check for available updates
     * @param onUpdateAvailable Callback with update info and priority
     */
    fun checkForUpdate(
        onUpdateAvailable: (updateInfo: AppUpdateInfo, updatePriority: Int) -> Unit = { _, _ -> },
        onNoUpdateAvailable: () -> Unit = {}
    ) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    val updatePriority = appUpdateInfo.updatePriority()
                    Timber.d("InAppUpdate: Update available (priority: $updatePriority)")
                    onUpdateAvailable(appUpdateInfo, updatePriority)
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    Timber.d("InAppUpdate: Update already in progress")
                    // Resume the update if it was interrupted
                    currentActivity?.let { activity ->
                        startImmediateUpdate(appUpdateInfo, activity)
                    }
                }
                else -> {
                    Timber.d("InAppUpdate: No update available")
                    onNoUpdateAvailable()
                }
            }
        }?.addOnFailureListener { exception ->
            Timber.e(exception, "InAppUpdate: Failed to check for updates")
        }
    }

    /**
     * Start flexible update (user can continue using app while downloading)
     */
    fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            Timber.d("InAppUpdate: Starting flexible update")
            try {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    REQUEST_CODE_FLEXIBLE_UPDATE
                )
            } catch (e: Exception) {
                Timber.e(e, "InAppUpdate: Failed to start flexible update")
            }
        } else {
            Timber.w("InAppUpdate: Flexible update not allowed")
        }
    }

    /**
     * Start immediate update (blocks app usage until update is completed)
     * Use this for critical/high-priority updates
     */
    fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            Timber.d("InAppUpdate: Starting immediate update")
            try {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    REQUEST_CODE_IMMEDIATE_UPDATE
                )
            } catch (e: Exception) {
                Timber.e(e, "InAppUpdate: Failed to start immediate update")
            }
        } else {
            Timber.w("InAppUpdate: Immediate update not allowed")
        }
    }

    /**
     * Complete a flexible update (call this after download is complete)
     */
    fun completeFlexibleUpdate() {
        appUpdateManager?.completeUpdate()?.addOnSuccessListener {
            Timber.d("InAppUpdate: Flexible update completed successfully")
        }?.addOnFailureListener { exception ->
            Timber.e(exception, "InAppUpdate: Failed to complete flexible update")
        }
    }

    /**
     * Resume update if it was interrupted
     * Call this in onResume()
     */
    fun resumeUpdateIfNeeded(activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Timber.d("InAppUpdate: Resuming update")
                startImmediateUpdate(appUpdateInfo, activity)
            }

            // Check if flexible update was downloaded while app was in background
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Timber.d("InAppUpdate: Flexible update ready to install")
                onUpdateDownloaded?.invoke()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager?.unregisterListener(installStateUpdatedListener)
        currentActivity = null
        Timber.d("InAppUpdate: Manager destroyed")
    }

    companion object {
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
        const val REQUEST_CODE_IMMEDIATE_UPDATE = 1002

        // Update priority thresholds
        const val PRIORITY_HIGH = 4 // Force immediate update
        const val PRIORITY_MEDIUM = 3 // Recommend update
        const val PRIORITY_LOW = 2 // Optional update
    }
}
