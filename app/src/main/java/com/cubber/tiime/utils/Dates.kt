package com.cubber.tiime.utils

import java.util.*

/**
 * Created by mike on 14/12/17.
 */
fun Date.toCal(transform: (Calendar.() -> Unit)) : Date {
    val cal = Calendar.getInstance()
    cal.clear()
    cal.time = this
    transform(cal)
    return cal.time
}