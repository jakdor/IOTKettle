package com.jakdor.iotkettle.mvp

abstract class BasePresenter<out View> protected constructor(protected val view: View) {

    /**
     * Common setup actions
     */
    fun start() {

    }

    /**
     * Common cleanup actions
     */
    fun stop() {

    }

    /**
     * Common cleanup actions
     */
    fun destroy() {

    }

    enum class RequestState {
        IDLE,
        LOADING,
        COMPLETE,
        ERROR
    }
}