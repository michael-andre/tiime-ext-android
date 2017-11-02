@file:JvmName("Formats")

package com.cubber.tiime.utils

import android.os.Build
import java.text.*
import java.util.*

/**
 * Created by mike on 27/09/17.
 */

fun fullDateFormat(): DateFormat {
    return dateFormat("EEEE d MMM yyyy")
}

fun shortDateFormat(): DateFormat {
    return dateFormat("d MMM")
}

fun dateFormat(pattern: String): DateFormat {
    val bestPattern = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)
    } else {
        pattern
    }
    return SimpleDateFormat(bestPattern, Locale.getDefault())
}

fun monthFormat(): Format {
    return capitalize(dateFormat("MMMM yyyy"))
}

fun euroFormat(): Format {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("EUR")
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 0
    return nullSafe(format)
}

private fun nullSafe(format: Format): Format {
    return object : Format() {

        override fun format(o: Any?, stringBuffer: StringBuffer, fieldPosition: FieldPosition): StringBuffer {
            return if (o != null) format.format(o, stringBuffer, fieldPosition) else stringBuffer
        }

        override fun parseObject(s: String, parsePosition: ParsePosition): Any {
            return format.parseObject(s, parsePosition)
        }

    }
}

fun capitalize(format: Format): Format {
    return object : Format() {

        override fun format(o: Any, stringBuffer: StringBuffer, fieldPosition: FieldPosition): StringBuffer {
            val sb = format.format(o, stringBuffer, fieldPosition)
            return sb.replace(0, 1, sb[0].toString().toUpperCase())
        }

        override fun parseObject(s: String, parsePosition: ParsePosition): Any {
            return format.parseObject(s, parsePosition)
        }

    }
}
