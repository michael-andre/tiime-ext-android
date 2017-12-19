package com.wapplix.binding

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView

import com.wapplix.recycler.ListAdapter

/**
 * Created by mike on 21/09/17.
 */

@BindingAdapter("items")
fun <T> setItems(recyclerView: RecyclerView, list: ObservablePagedList<T>?) {
    val adapter = recyclerView.adapter
    if (adapter is PagedListAdapter<*, *>) {
        @Suppress("unchecked_cast")
        (adapter as PagedListAdapter<T, *>).setList(list?.pagedList)
    }
}

@BindingAdapter("items")
fun <T> setItems(recyclerView: RecyclerView, list: PagedList<T>?) {
    val adapter = recyclerView.adapter
    if (adapter is PagedListAdapter<*, *>) {
        @Suppress("unchecked_cast")
        (adapter as PagedListAdapter<T, *>).setList(list)
    }
}

@BindingAdapter("items")
fun <T> setItems(recyclerView: RecyclerView, list: List<T>?) {
    val adapter = recyclerView.adapter
    if (adapter is ListAdapter<*, *>) {
        @Suppress("unchecked_cast")
        (adapter as ListAdapter<T, *>).items = list
    }
}