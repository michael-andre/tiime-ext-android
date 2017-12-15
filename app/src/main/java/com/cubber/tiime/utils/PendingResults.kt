package com.cubber.tiime.utils

import com.google.maps.PendingResult

/**
 * Created by mike on 12/12/17.
 */
fun <T> PendingResult<T>.setCallback(onResult: (T) -> Unit, onFailure: (Throwable?) -> Unit) {
    setCallback(object : PendingResult.Callback<T> {

        override fun onResult(result: T) = onResult.invoke(result)

        override fun onFailure(e: Throwable?) = onFailure.invoke(e)

    })

}