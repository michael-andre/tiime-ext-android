package com.wapplix.binding

import android.databinding.InverseBindingAdapter
import com.wapplix.widget.NumberEditText

/**
 * Created by Mike on 28/10/2016.
 */
@InverseBindingAdapter(attribute = "value", event = "android:textAttrChanged")
fun getValue(view: NumberEditText): Number? {
    return view.value
}

@InverseBindingAdapter(attribute = "value", event = "android:textAttrChanged")
fun getFloatValue(view: NumberEditText): Int? {
    val value = view.value
    return value?.toInt()
}
