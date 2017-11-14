package com.wapplix.recycler

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.Dimension
import android.support.annotation.StyleRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

/**
 * Created by mike on 08/11/2017.
 */
class AutoGridLayoutManager : GridLayoutManager {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val values = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.columnWidth), defStyleAttr, defStyleRes)
        columnWidth = values.getDimensionPixelSize(0, -1)
        values.recycle()
    }

    constructor(context: Context, @Dimension columnWidth: Int) : super(context, 1) {
        this.columnWidth = columnWidth
    }

    constructor(context: Context, @Dimension columnWidth: Int, orientation: Int, reverseLayout: Boolean) : super(context, 1, orientation, reverseLayout) {
        this.columnWidth = columnWidth
    }

    @Dimension
    var columnWidth: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(recycler: RecyclerView.Recycler?, state: RecyclerView.State?, widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        if (columnWidth > 0) {
            val spanCount = Math.max(1, width / columnWidth)
            setSpanCount(spanCount)
        }
    }

}