package com.wapplix.arch

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner

/**
 * Created by mike on 11/12/17.
 */
fun LifecycleOwner.runOnStarted(action: () -> Unit) {
    lifecycle.addObserver(object : GenericLifecycleObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val state = source.lifecycle.currentState
            if (state.isAtLeast(Lifecycle.State.STARTED)) {
                source.lifecycle.removeObserver(this)
                action()
            } else if (state == Lifecycle.State.DESTROYED) {
                source.lifecycle.removeObserver(this)
            }
        }

    })
}