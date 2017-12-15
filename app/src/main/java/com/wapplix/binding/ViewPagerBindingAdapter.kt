package com.wapplix.binding

import android.databinding.BindingAdapter
import android.support.v4.view.ViewPager
import com.wapplix.pager.FragmentStateListAdapter

/**
 * Created by mike on 14/12/17.
 */
@BindingAdapter("items")
fun <TItem> setItems(adapterView: ViewPager, items: List<TItem>?) {
    val adapter = adapterView.adapter
    if (adapter is FragmentStateListAdapter<*>) {
        @Suppress("unchecked_cast")
        (adapter as FragmentStateListAdapter<TItem>).items = items
    } else if (adapter != null) {
        error("Unable to bind items to adapter: $adapter")
    }
}