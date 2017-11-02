package com.wapplix.recycler

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Mike on 03/10/2016.
 */

abstract class BindingListAdapter<T, VDB : ViewDataBinding> : ListAdapter<T, BindingViewHolder<VDB>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<VDB> {
        return BindingViewHolder(onCreateViewBinding(LayoutInflater.from(parent.context), parent, viewType))
    }

    protected abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VDB

    override fun onBindViewHolder(holder: BindingViewHolder<VDB>, item: T) {
        onBindView(holder.binding, item)
        holder.binding.executePendingBindings()
    }

    protected abstract fun onBindView(binding: VDB, item: T)


}
