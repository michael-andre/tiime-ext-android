package com.wapplix.widget

import android.content.Context
import android.content.res.Resources
import android.support.annotation.LayoutRes
import android.support.v7.widget.ThemedSpinnerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Mike on 27/10/2016.
 */

abstract class SimpleAdapter<T> @JvmOverloads constructor(
        context: Context,
        @LayoutRes private val viewResource: Int,
        @LayoutRes private var dropDownViewResource: Int = viewResource
) : ListAdapter<T>(), ThemedSpinnerAdapter {

    private val dropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    constructor(context: Context) : this(context, android.R.layout.simple_spinner_item, android.R.layout.simple_spinner_dropdown_item)

    fun setDropDownViewResource(@LayoutRes dropDownViewResource: Int) {
        this.dropDownViewResource = dropDownViewResource
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(viewResource, parent, false)
    }

    override fun onCreateDropDownView(parent: ViewGroup, viewType: Int): View {
        return dropDownHelper.dropDownViewInflater.inflate(dropDownViewResource, parent, false)
    }

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        dropDownHelper.dropDownViewTheme = theme
    }

    override fun getDropDownViewTheme(): Resources.Theme? {
        return dropDownHelper.dropDownViewTheme
    }

}
