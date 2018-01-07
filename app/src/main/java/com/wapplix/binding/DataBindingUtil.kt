package com.wapplix.binding

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes

/**
 * Created by mike on 12/12/17.
 */
fun <T : ViewDataBinding> Activity.setContentViewBinding(@LayoutRes layoutId: Int): T =
        DataBindingUtil.setContentView(this, layoutId) ?: error("The layout is not a data binding layout")