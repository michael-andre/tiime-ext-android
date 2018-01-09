package com.wapplix.arch

import android.arch.core.util.Function
import android.arch.lifecycle.*
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.reactivestreams.Publisher

/**
 * Created by mike on 01/11/17.
 */
inline fun <T> MutableLiveData<T>.update(block: T.() -> Unit) {
    val value = value
    if (value != null) {
        block(value)
        this.postValue(value)
    }
}

fun <T> LiveData<T>.singleLoad(): LiveData<T> = SingleLoadData(this)

fun <T, U> LiveData<T>.switchMap(mapper: (T) -> LiveData<U>?): LiveData<U> {
    return Transformations.switchMap(this, mapper)
}

inline fun <T> LiveData<T>.observe(owner: LifecycleOwner, crossinline observer: (T?) -> Unit) = observe(owner, Observer { observer.invoke(it) })

inline fun <T> LifecycleOwner.observe(data: LiveData<T>, crossinline observer: (T?) -> Unit) = data.observe(this, observer)

fun <T, U> LiveData<T>.map(mapper: (T) -> U): LiveData<U> {
    return Transformations.map(this, mapper)
}

fun <T> Publisher<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)
fun <T> Observable<T>.toLiveData(strategy: BackpressureStrategy = BackpressureStrategy.LATEST, onError: Consumer<Throwable>? = null): LiveData<T> =
        this.onErrorResumeNext { e: Throwable ->
            onError?.accept(e)
            Log.e("LiveData", "Observable error", e)
            Observable.empty()
        }.toFlowable(strategy).toLiveData()

fun <T> Single<T>.toLiveData(onError: Consumer<Throwable>? = null): LiveData<T> = this.toObservable().toLiveData(onError = onError)
fun <T> Maybe<T>.toLiveData(onError: Consumer<Throwable>? = null): LiveData<T> = this.toObservable().toLiveData(onError = onError)

fun <T, U, R> LiveData<T>.cancellingSwitchMap(mapper: (T?) -> R?): LiveData<U> where R : LiveData<U>, R : Cancelable {
    return Transformations.switchMap(this, object : Function<T, LiveData<U>?> {

        private var current: Cancelable? = null

        override fun apply(input: T): LiveData<U>? {
            val newResult = mapper(input)
            current?.let {
                if (it != newResult) it.cancel()
            }
            current = newResult
            return newResult
        }

    })
}

interface Cancelable {
    fun cancel()
}

class SimpleData<T>(value: T) : LiveData<T>() {
    init {
        setValue(value)
    }
}