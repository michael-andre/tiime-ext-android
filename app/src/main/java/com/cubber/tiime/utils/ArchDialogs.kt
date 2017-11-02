package com.cubber.tiime.utils

import android.arch.lifecycle.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Created by mike on 27/09/17.
 */

object ArchDialogs {

    interface Emitter<T>

    fun <T, TEmitter> sendResult(emitter: TEmitter, result: T) where TEmitter : Fragment, TEmitter : ArchDialogs.Emitter<T> {
        val receiver = emitter.targetFragment
        if (receiver != null) {
            getResult<Any>(receiver, emitter.tag).postValue(result)
        } else {
            getResult<Any>(emitter.activity!!, emitter.tag).postValue(result)
        }
    }

    fun <T> resultOf(emitter: Class<out Emitter<T>>, tag: String, receiver: Fragment): LiveData<T> {
        return getResult(receiver, tag)
    }

    fun <T> resultOf(emitter: Class<out Emitter<T>>, tag: String, receiver: FragmentActivity): LiveData<T> {
        return getResult(receiver, tag)
    }

    private fun <T> getResult(receiver: Fragment, tag: String?): MutableLiveData<T> {
        return getResult(ViewModelProviders.of(receiver), tag)
    }

    private fun <T> getResult(receiver: FragmentActivity, tag: String?): MutableLiveData<T> {
        return getResult(ViewModelProviders.of(receiver), tag)
    }

    private fun <T> getResult(provider: ViewModelProvider, tag: String?): MutableLiveData<T> {
        @Suppress("UNCHECKED_CAST")
        return provider.get(tag!!, ResultModel::class.java).result as MutableLiveData<T>
    }

    class ResultModel<T> : ViewModel() {
        internal var result = MutableLiveData<T>()
    }

}
