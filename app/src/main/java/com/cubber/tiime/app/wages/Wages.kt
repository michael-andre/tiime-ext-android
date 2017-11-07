package com.cubber.tiime.app.wages

import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.shortDateFormat
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

object Wages {

    fun getHolidaysSummary(wage: Wage): Map<String?, Double> {
        val summary = HashMap<String?, Double>()
        val holidays = wage.holidays
        if (holidays != null) {
            for ((_, _, type, duration) in holidays) {
                var total: Double? = summary[type]
                total = if (total == null) duration / 2.0 else total + duration / 2.0
                summary.put(type, total)
            }
        }
        return summary
    }

    fun getShortDatesSummary(startDate: Date, duration: Int): String {
        return if (duration < 3) {
            shortDateFormat().format(startDate)
        } else {
            val cal = Calendar.getInstance()
            cal.time = startDate
            cal.add(Calendar.DAY_OF_MONTH, duration / 2)
            (shortDateFormat().format(startDate)
                    + " → "
                    + shortDateFormat().format(cal.time))
        }
    }

}

fun Iterable<Wage>.firstEditable(holidayDate: Date): Wage? {
    val indexed = this.associateBy { it.period }
    val cal = Calendar.getInstance()
    cal.time = holidayDate
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.MILLISECOND, 0)
    indexed[cal.time]?.let {
        if (it.editable) return it
    }
    cal.add(Calendar.MONTH, 1)
    indexed[cal.time]?.let {
        if (it.editable) return it
    }
    return null
}