package com.wapplix.widget

import android.support.annotation.MenuRes
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.View
import com.cubber.tiime.R

/**
 * Created by mike on 06/12/17.
 */
fun View.setPopupMenu(@MenuRes menu: Int, gravity: Int = Gravity.BOTTOM or Gravity.END, onMenuClick: (itemId: Int) -> Unit) =
        setPopupMenu(PopupMenu(context, this, gravity), menu, onMenuClick)

fun View.setOverflowPopupMenu(@MenuRes menu: Int, gravity: Int = Gravity.BOTTOM or Gravity.END, onMenuClick: (itemId: Int) -> Unit) =
        setPopupMenu(PopupMenu(context, this, gravity, 0, R.style.Widget_AppCompat_PopupMenu_Overflow), menu, onMenuClick)

fun View.setPopupMenu(popup: PopupMenu, @MenuRes menu: Int, onMenuClick: (itemId: Int) -> Unit): PopupMenu {
    popup.inflate(menu)
    popup.setOnMenuItemClickListener { item ->
        onMenuClick(item.itemId)
        true
    }
    setOnTouchListener(popup.dragToOpenListener)
    setOnClickListener { popup.show() }
    return popup
}