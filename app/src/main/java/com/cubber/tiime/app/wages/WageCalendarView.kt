package com.cubber.tiime.app.wages

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import com.cubber.tiime.R
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener

/**
 * Created by mike on 30/11/17.
 */
class WageCalendarView(context: Context?, attrs: AttributeSet? = null) : MaterialCalendarView(context, attrs) {

    private var longPressFlag = false
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onLongPress(e: MotionEvent?) {
            longPressFlag = true
        }

    })

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            longPressFlag = false
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun setOnDateChangedListener(listener: OnDateSelectedListener?) {
        super.setOnDateChangedListener { widget, date, selected ->
            if (longPressFlag) {
                selectionMode = SELECTION_MODE_RANGE
                setDateSelected(date, true)
                Snackbar.make(this, R.string.select_end_date, Toast.LENGTH_SHORT)
                        .setAction(android.R.string.cancel) {
                            selectionMode = SELECTION_MODE_SINGLE
                            listener?.onDateSelected(widget, date, selected)
                        }
                        .show()
            } else {
                listener?.onDateSelected(widget, date, selected)
            }
        }
    }

    override fun setOnRangeSelectedListener(listener: OnRangeSelectedListener?) {
        super.setOnRangeSelectedListener { widget, dates ->
            selectionMode = SELECTION_MODE_SINGLE
            listener?.onRangeSelected(widget, dates)
        }
    }

}