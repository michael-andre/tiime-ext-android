package com.wapplix.maps

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations

import com.google.android.gms.common.api.Result
import com.google.maps.PendingResult

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by mike on 04/10/17.
 */

open class PendingResultData<T>(private val mPendingResult: PendingResult<T>) : LiveData<T>(), PendingResult.Callback<T> {
    private val mStarted = AtomicBoolean(false)

    override fun onActive() {
        super.onActive()
        if (mStarted.compareAndSet(false, true)) {
            mPendingResult.setCallback(this)
        }
    }

    override fun onResult(result: T) {
        postValue(result)
    }

    override fun onFailure(e: Throwable) {

    }

    companion object {

        fun <U, T : Result> cancellingSwitchMap(trigger: LiveData<U>, func: Function<U, PendingResult<T>>): LiveData<T> {
            return Transformations.switchMap(trigger, object : Function<U, LiveData<T>> {

                internal var mResult: PendingResult<T>? = null

                override fun apply(input: U): LiveData<T> {
                    val newResult = func.apply(input)
                    if (mResult != null && mResult != newResult) {
                        mResult!!.cancel()
                    }
                    return PendingResultData(newResult)
                }

            })
        }
    }

}
