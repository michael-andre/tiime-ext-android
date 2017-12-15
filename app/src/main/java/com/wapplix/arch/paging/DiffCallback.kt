package com.wapplix.arch.paging

import android.support.v7.recyclerview.extensions.DiffCallback

/**
 * Created by mike on 13/12/17.
 */
fun <T> diffCallbackBy(id: T.() -> Any) = object : DiffCallback<T>() {

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return id(oldItem) == id(newItem)
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

}