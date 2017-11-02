package com.wapplix.gms

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations

import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.ResultCallback

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by mike on 29/09/17.
 */

class PendingResultData<T : Result>(private val pendingResult: PendingResult<T>) : LiveData<T>(), ResultCallback<T> {

    private val started = AtomicBoolean(false)

    override fun onActive() {
        super.onActive()
        if (started.compareAndSet(false, true)) {
            pendingResult.setResultCallback(this)
        }
    }

    override fun onResult(result: T) {
        postValue(result)
    }

    companion object {

        fun <U, T : Result> cancellingSwitchMap(trigger: LiveData<U>, switch: (U) -> PendingResult<T>): LiveData<T> {
            return Transformations.switchMap(trigger, object : Function<U, LiveData<T>> {

                internal var result: PendingResult<T>? = null

                override fun apply(input: U): LiveData<T> {
                    val newResult = switch(input)
                    if (result != null && result != newResult) {
                        result!!.cancel()
                    }
                    return PendingResultData(newResult)
                }

            })
        }
    }

}
