package net.gotev.uploadservice.observer.request

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.UploadServiceConfig

class LifeCycleRequestObserver(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val delegate: RequestObserverDelegate
) : ManualRequestObserver(context, delegate) {

    private var subscribedUploadID: String? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * Register this upload receiver to listen for events.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun register() {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(this, UploadServiceConfig.broadcastStatusIntentFilter)

        subscribedUploadID?.let {
            if (!UploadService.taskList.contains(it)) {
                delegate.onCompletedWhileNotObserving()
            }
        }
    }

    /**
     * Unregister this upload receiver from listening events.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun unregister() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
    }

}
