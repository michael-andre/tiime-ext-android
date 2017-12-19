package com.wapplix.binding

import android.arch.paging.PagedList
import android.databinding.BaseObservable

/**
 * Created by mike on 19/12/17.
 */
class ObservablePagedList<T>(val pagedList: PagedList<T>) : BaseObservable(), List<T> by pagedList {

    private val callback = object : PagedList.Callback() {

        override fun onChanged(position: Int, count: Int) { notifyChange() }

        override fun onInserted(position: Int, count: Int) { notifyChange() }

        override fun onRemoved(position: Int, count: Int) { notifyChange() }

    }

    init {
        pagedList.addWeakCallback(null, callback)
    }

}

fun <T> PagedList<T>.toObservable(): ObservablePagedList<T>
    = ObservablePagedList(this)