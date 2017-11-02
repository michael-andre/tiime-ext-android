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
    return this.firstOrNull {
        if (!it.editable) return@firstOrNull false
        if (!it.period!!.before(holidayDate)) return@firstOrNull true
        val cal = Calendar.getInstance()
        cal.time = it.period
        cal.add(Calendar.MONTH, -1)
        if (!it.period!!.before(holidayDate)) return@firstOrNull true
        false
    }
}