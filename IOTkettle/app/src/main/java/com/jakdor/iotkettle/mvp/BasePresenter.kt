package com.jakdor.iotkettle.mvp

abstract class BasePresenter<out View> protected constructor(protected val view: View) {

    /**
     * Common setup actions
     */
    open fun start() {

    }

    /**
     * Common cleanup actions
     */
    open fun stop() {

    }

    /**
     * Common cleanup actions
     */
    open fun destroy() {

    }

    enum class RequestState {
        IDLE,
        LOADING,
        COMPLETE,
        ERROR
    }
}