package com.wapplix.arch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource

abstract class StatefulDataSourceFactory<K, V> : DataSource.Factory<K, V> {

    protected val internalState = MutableLiveData<State>()
    val state : LiveData<State> = internalState

}