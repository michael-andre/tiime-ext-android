package com.wapplix.widget

import android.content.Context
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.ThemedSpinnerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Mike on 27/10/2016.
 */

abstract class BindingListAdapter<T, VDB : ViewDataBinding, DropDownVDB : ViewDataBinding>(context: Context) : ListAdapter<T>(), ThemedSpinnerAdapter {

    private val dropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return onCreateViewBinding(inflater, parent, viewType).root
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VDB

    override fun onBindView(view: View, item: T) {
        val binding = DataBindingUtil.getBinding<VDB>(view) ?: error("Binding not found")
        onBindView(binding, item)
        binding.executePendingBindings()
    }

    abstract fun onBindView(binding: VDB, item: T)

    override fun onCreateDropDownView(parent: ViewGroup, viewType: Int): View {
        return onCreateDropDownViewBinding(dropDownHelper.dropDownViewInflater, parent, viewType).root
    }

    abstract fun onCreateDropDownViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): DropDownVDB

    override fun onBindDropDownView(view: View, item: T) {
        val binding = DataBindingUtil.getBinding<DropDownVDB>(view) ?: error("Binding not found")
        onBindDropDownView(binding, item)
        binding.executePendingBindings()
    }

    abstract fun onBindDropDownView(binding: DropDownVDB, item: T)

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        dropDownHelper.dropDownViewTheme = theme
    }

    override fun getDropDownViewTheme(): Resources.Theme? {
        return dropDownHelper.dropDownViewTheme
    }

}
