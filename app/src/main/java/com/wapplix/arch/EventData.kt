package com.wapplix.arch

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import android.util.Log

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by mike on 03/10/17.
 */

class EventData<T> : LiveData<T>() {

    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }
        // Observe the internal MutableLiveData
        super.observe(owner, Observer { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    @AnyThread
    fun trigger(t: T?) {
        postValue(t)
    }

    @AnyThread
    fun trigger() {
        postValue(null)
    }

    companion object {

        private val TAG = "SingleLiveEvent"
    }

}
