package net.gotev.uploadservice.observer.request

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.parcel.Parcelize
import net.gotev.uploadservice.UploadRequest
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.BroadcastData
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadStatus
import net.gotev.uploadservice.network.ServerResponse

class CustomManualRequestObserver(
        private val context: Context,
        private val uploadID: String,
        private val delegate: RequestObserverDelegate
) : BroadcastReceiver(), RequestObserver {

    override fun onReceive(context: Context, intent: Intent?) {
        val safeIntent = intent ?: return
        if (safeIntent.action != UploadServiceConfig.broadcastStatusAction) return
        val data = BroadcastData.fromIntent(safeIntent)
                ?: return

        val uploadInfo = data.uploadInfo

        if (!shouldAcceptEventFrom(uploadInfo)) {
            return
        }

        when (data.status) {
            UploadStatus.InProgress -> delegate.onProgress(context, uploadInfo)
            UploadStatus.Error -> delegate.onError(context, uploadInfo, data.exception!!)
            UploadStatus.Success -> delegate.onSuccess(context, uploadInfo, data.serverResponse!!)
            UploadStatus.Completed -> {
                delegate.onCompleted(context, uploadInfo)
                unregister()
            }
        }
    }

    /**
     * Method called every time a new event arrives from an upload task, to decide whether or not
     * to process it. If this request observer subscribed a particular upload task, it will listen
     * only to it
     *
     * @param uploadInfo upload info to
     * @return true to accept the event, false to discard it
     */
    private fun shouldAcceptEventFrom(uploadInfo: UploadInfo): Boolean {
        val uploadId = uploadID ?: return true
        return uploadId == uploadInfo.uploadId
    }

    /**
     * Register this upload receiver to listen for events.
     */
    override fun register() {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, UploadServiceConfig.broadcastStatusIntentFilter)

        if (!UploadService.taskList.contains(uploadID)) {
            delegate.onCompletedWhileNotObserving()
        }
    }

    /**
     * Unregister this upload receiver from listening events.
     */
    override fun unregister() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
    }

    override fun subscribe(request: UploadRequest<*>) {
        error("Not working for this class")
    }
}
