package com.wapplix.arch

import android.arch.lifecycle.LiveData
import io.reactivex.Single
import io.reactivex.SingleSource

import io.reactivex.SingleTransformer

/**
 * Created by mike on 06/12/17.
 */

class ProgressData<T> : LiveData<Boolean>(), SingleTransformer<T, T> {

    override fun apply(upstream: Single<T>): SingleSource<T> = upstream
            .doOnSubscribe { postValue(true) }
            .doAfterSuccess { postValue(false) }

}
