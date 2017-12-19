package com.cubber.tiime.utils

import java.util.*

/**
 * Created by mike on 19/12/17.
 */
class Month(time: Long) : Date(time)

val Calendar.month: Month
    get() = Month(timeInMillis)

fun Date.toMonth() = Month(this.time)