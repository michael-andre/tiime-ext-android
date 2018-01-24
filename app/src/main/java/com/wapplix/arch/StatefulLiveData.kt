package com.wapplix.arch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.reactivestreams.Publisher

open class StatefulLiveData<T> : MediatorLiveData<T>() {

    var state = MutableLiveData<State>()

}

fun <T> Publisher<T>.toStatefulLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)
fun <T> Observable<T>.toStatefulLiveData(strategy: BackpressureStrategy = BackpressureStrategy.LATEST): StatefulLiveData<T> =
        object : StatefulLiveData<T>() {

            init { connect() }

            private fun connect() {
                addSource(LiveDataReactiveStreams.fromPublisher(
                        this@toStatefulLiveData.onErrorResumeNext { e: Throwable ->
                            state.postValue(Error(e) { connect() })
                            Observable.empty()
                        }.toFlowable(strategy))
                ) { value = it }
            }

        }

fun <T> Single<T>.toStatefulLiveData(): StatefulLiveData<T> = this.toObservable().toStatefulLiveData()
fun <T> Maybe<T>.toStatefulLiveData(): StatefulLiveData<T> = this.toObservable().toStatefulLiveData()
