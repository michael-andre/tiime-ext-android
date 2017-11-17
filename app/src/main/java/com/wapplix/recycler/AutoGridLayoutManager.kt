package com.wapplix.recycler

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Dimension
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.wapplix.utils.createParcel

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

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (columnWidth > 0) {
            val c = Math.max(1, width / columnWidth)
            if (spanCount != c) {
                spanCount = c
            }
        }
        super.onLayoutChildren(recycler, state)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState())
                .also { it.columnWidth = columnWidth }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            columnWidth = state.columnWidth
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    class SavedState : View.BaseSavedState {

        var columnWidth: Int = 0

        constructor(superState: Parcelable) : super(superState)
        private constructor(source: Parcel) : super(source) {
            columnWidth = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(columnWidth)
        }

        companion object {

            @JvmField @Suppress("unused")
            val CREATOR = createParcel { SavedState(it) }

        }

    }

}