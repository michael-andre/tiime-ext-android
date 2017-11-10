package com.wapplix.gms

import android.arch.lifecycle.LiveData
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.ResultCallback
import com.wapplix.arch.Cancelable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by mike on 29/09/17.
 */

class PendingResultData<T : Result>(
        private val pendingResult: PendingResult<T>
) : LiveData<T>(), ResultCallback<T>, Cancelable {

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

    override fun cancel() {
        pendingResult.cancel()
    }

}

fun <T : Result> PendingResult<T>.toData(): PendingResultData<T> {
    return PendingResultData(this)
}