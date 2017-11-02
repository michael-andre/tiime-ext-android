package com.wapplix.binding

import android.databinding.BindingAdapter
import android.support.v4.widget.ContentLoadingProgressBar
import android.view.View

/**
 * Created by Mike on 14/10/2016.
 */
@BindingAdapter("visible")
fun setVisible(view: ContentLoadingProgressBar, visible: Boolean) {
    view.visibility = View.VISIBLE
    if (visible)
        view.show()
    else
        view.hide()
}