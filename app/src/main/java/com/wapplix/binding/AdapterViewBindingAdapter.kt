package com.wapplix.binding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.adapters.AdapterViewBindingAdapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.wapplix.widget.ListAdapter

/**
 * Created by mike on 11/10/17.
 */
@BindingAdapter("items")
fun <TItem> setItems(adapterView: AdapterView<*>, items: List<TItem>?) {
    val adapter = adapterView.adapter
    if (adapter is ListAdapter<*>) {
        @Suppress("unchecked_cast")
        (adapter as ListAdapter<TItem>).items = items
    } else if (adapter is ArrayAdapter<*>) {
        @Suppress("unchecked_cast")
        val arrayAdapter = adapter as ArrayAdapter<TItem>
        arrayAdapter.clear()
        if (items != null) arrayAdapter.addAll(items)
    }
}

@BindingAdapter("android:selectedItem")
fun <T> setSelectedItem(adapterView: AdapterView<*>, item: T) {
    val adapter = adapterView.adapter ?: return
    var position = -1
    if (adapter is ListAdapter<*>) {
        @Suppress("unchecked_cast")
        position = (adapter as ListAdapter<in T>).getItemPosition(item)
    } else if (adapter is ArrayAdapter<*>) {
        @Suppress("unchecked_cast")
        position = (adapter as ArrayAdapter<in T>).getPosition(item)
    }
    AdapterViewBindingAdapter.setSelectedItemPosition(adapterView, position)
}

@InverseBindingAdapter(attribute = "android:selectedItem", event = "android:selectedItemPositionAttrChanged")
fun getSelectedItem(adapterView: AdapterView<*>): Any {
    return adapterView.selectedItem
}
