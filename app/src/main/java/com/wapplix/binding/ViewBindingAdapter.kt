package com.wapplix.binding

import android.databinding.BindingAdapter
import android.view.View

/**
 * Created by Mike on 14/10/2016.
 */

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}
