package net.gotev.uploadservice.observer.request

import net.gotev.uploadservice.UploadRequest

interface RequestObserver {

    /**
     * Register this upload receiver to listen for events.
     */
    fun register()

    /**
     * Unregister this upload receiver from listening events.
     */
    fun unregister()

    /**
     * Subscribe to get only the events from the given upload request. Otherwise, it will listen to
     * all the upload requests.
     */
    fun subscribe(request: UploadRequest<*>)
}