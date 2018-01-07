package com.wapplix.widget

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Mike on 28/10/2016.
 */

class SimpleBindingListAdapter<T> @JvmOverloads constructor(
        context: Context,
        @param:LayoutRes @field:LayoutRes private val viewResource: Int,
        @param:LayoutRes @field:LayoutRes private val dropDownViewResource: Int = viewResource,
        private val variableId: Int
) : BindingListAdapter<T, ViewDataBinding, ViewDataBinding>(context) {

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(inflater, viewResource, parent, false)!!
    }

    override fun onBindView(binding: ViewDataBinding, item: T) {
        if (variableId != 0) binding.setVariable(variableId, item)
    }

    override fun onCreateDropDownViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(inflater, dropDownViewResource, parent, false)!!
    }

    override fun onBindDropDownView(binding: ViewDataBinding, item: T) {
        onBindView(binding, item)
    }

}