package com.denisu.homesorter.model

import com.denisu.homesorter.DataCallback

object CallbackManager {
    private var callbackListener: DataCallback? = null

    fun setCallbackListener(listener: DataCallback) {
        callbackListener = listener
    }

    fun getCallbackListener(): DataCallback? {
        return callbackListener
    }

    private var callbackListenerItem: DataCallback? = null

    fun setCallbackListenerItem(listener: DataCallback) {
        callbackListenerItem = listener
    }

    fun getCallbackListenerItem(): DataCallback? {
        return callbackListenerItem
    }
}
