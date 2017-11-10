package com.wapplix.maps

import android.arch.lifecycle.LiveData
import com.google.maps.PendingResult
import com.wapplix.arch.Cancelable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by mike on 04/10/17.
 */

open class PendingResultData<T>(
        private val pendingResult: PendingResult<T>
) : LiveData<T>(), PendingResult.Callback<T>, Cancelable {

    private val started = AtomicBoolean(false)

    override fun onActive() {
        super.onActive()
        if (started.compareAndSet(false, true)) {
            pendingResult.setCallback(this)
        }
    }

    override fun onResult(result: T) {
        postValue(result)
    }

    override fun onFailure(e: Throwable) {

    }

    override fun cancel() {
        pendingResult.cancel()
    }

}

fun <T> PendingResult<T>.toData(): PendingResultData<T> {
    return PendingResultData(this)
}
