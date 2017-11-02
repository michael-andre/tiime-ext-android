package com.wapplix.widget

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet

/**
 * Created by mike on 02/10/17.
 */

class InstantAutoCompleteTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.support.v7.appcompat.R.attr.autoCompleteTextViewStyle
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun setThreshold(threshold: Int) {
        super.setThreshold(0)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            showDropDown()
            performFiltering(text, 0)
        }
    }

}
