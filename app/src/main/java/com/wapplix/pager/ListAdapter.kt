package com.wapplix.pager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by mike on 14/12/17.
 */
abstract class FragmentStateListAdapter<T>(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var items: List<T>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items?.size ?: 0

    override fun getItem(position: Int): Fragment = onCreateFragment(items!![position])

    abstract fun onCreateFragment(item: T): Fragment

}