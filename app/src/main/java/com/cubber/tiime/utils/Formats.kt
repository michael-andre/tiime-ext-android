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
    return dateFormat("MMMM yyyy").capitalized()
}

fun euroFormat(): Format {
    return NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("EUR")
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.nullSafe()
}

fun Format.nullSafe(): Format {
    return object : Format() {

        override fun format(o: Any?, stringBuffer: StringBuffer, fieldPosition: FieldPosition): StringBuffer {
            return if (o != null) this@nullSafe.format(o, stringBuffer, fieldPosition) else stringBuffer
        }

        override fun parseObject(s: String, parsePosition: ParsePosition): Any {
            return this@nullSafe.parseObject(s, parsePosition)
        }

    }
}

fun Format.capitalized(): Format {
    return object : Format() {

        override fun format(o: Any, stringBuffer: StringBuffer, fieldPosition: FieldPosition): StringBuffer {
            val sb = this@capitalized.format(o, stringBuffer, fieldPosition)
            return sb.replace(0, 1, sb[0].toString().toUpperCase())
        }

        override fun parseObject(s: String, parsePosition: ParsePosition): Any {
            return this@capitalized.parseObject(s, parsePosition)
        }

    }
}
