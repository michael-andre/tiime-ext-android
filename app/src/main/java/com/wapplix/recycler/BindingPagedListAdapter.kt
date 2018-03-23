package com.wapplix.recycler

import android.arch.paging.PagedListAdapter
import android.databinding.ViewDataBinding
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Mike on 03/10/2016.
 */

abstract class BindingPagedListAdapter<T, VDB : ViewDataBinding> : PagedListAdapter<T, BindingViewHolder<VDB>> {

    constructor(diffCallback: DiffCallback<T>) : super(diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<VDB> {
        return BindingViewHolder(onCreateViewBinding(LayoutInflater.from(parent.context), parent, viewType))
    }

    protected abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VDB

    override fun onBindViewHolder(holder: BindingViewHolder<VDB>, position: Int) {
        val item = getItem(position)
        if (item != null) onBindViewHolder(holder, item)
    }

    open fun onBindViewHolder(holder: BindingViewHolder<VDB>, item: T) {
        onBindView(holder.binding, item)
        holder.binding.executePendingBindings()
    }

    protected abstract fun onBindView(binding: VDB, item: T)

}
