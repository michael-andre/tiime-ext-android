package com.wapplix.arch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

/**
 * Created by mike on 04/12/17.
 */
class SingleLoadData<T>(source: LiveData<T>) : MediatorLiveData<T>() {

    init {
        addSource(source) { newValue ->
            removeSource(source)
            value = newValue
        }
    }

}

fun <T> LiveData<T>.toSingle() = SingleLoadData(this)