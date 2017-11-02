package com.wapplix.arch

import android.arch.lifecycle.MutableLiveData

/**
 * Created by mike on 01/11/17.
 */
inline fun <T> MutableLiveData<T>.update(block: T.() -> Unit) {
    val value = value
    if (value != null) {
        block(value)
        this.value = value
    }
}
