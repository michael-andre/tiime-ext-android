package com.wapplix.widget

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.google.common.base.Objects

/**
 * Created by Mike on 27/10/2016.
 */

abstract class ListAdapter<T> : BaseAdapter() {

    var items: List<T>? = null
        set(items) {
            val changed = !Objects.equal(items, this.items)
            field = items
            if (changed) notifyDataSetChanged()
        }

    override fun getItem(i: Int): T {
        return items!![i]
    }

    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItemPosition(item: T): Int {
        return items?.indexOf(item) ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = onCreateView(parent, getItemViewType(position))
        }
        onBindView(view, position)
        return view
    }

    abstract fun onCreateView(parent: ViewGroup, viewType: Int): View

    fun onBindView(view: View, position: Int) {
        onBindView(view, getItem(position))
    }

    abstract fun onBindView(view: View, item: T)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = onCreateDropDownView(parent, getItemViewType(position))
        }
        onBindDropDownView(view, position)
        return view
    }

    open fun onCreateDropDownView(parent: ViewGroup, viewType: Int): View {
        return onCreateView(parent, viewType)
    }

    open fun onBindDropDownView(view: View, position: Int) {
        onBindDropDownView(view, getItem(position))
    }

    open fun onBindDropDownView(view: View, item: T) {
        onBindView(view, item)
    }

}