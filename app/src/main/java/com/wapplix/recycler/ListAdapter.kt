package com.wapplix.recycler

import android.support.v7.widget.RecyclerView

abstract class ListAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var items: List<T>? = null
        set(items) {
            val changed = items != field || true
            field = items
            if (changed) notifyDataSetChanged()
        }

    fun getItem(position: Int): T {
        return items!![position]
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, getItem(position))
    }

    abstract fun onBindViewHolder(holder: VH, item: T)

    fun getIdPosition(id: Long): Int {
        return items?.indices?.first { getItemId(it) == id } ?: -1
    }

    fun getItemPosition(item: T): Int {
        return items?.indexOf(item) ?: -1
    }

}