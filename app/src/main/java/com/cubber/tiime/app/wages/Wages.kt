package com.cubber.tiime.app.wages

import com.cubber.tiime.model.Holiday
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.shortDateFormat
import java.util.*
import kotlin.math.ceil

/**
 * Created by mike on 26/10/17.
 */

object Wages {

    const val MAX_ALLOW_HOLIDAY_ON_PAST_MONTH = 1

    fun isEditable(wage: Wage) =
            wage.status == Wage.STATUS_EDITING
                    || wage.status == Wage.STATUS_VALIDATION_REQUIRED
                    || wage.status == Wage.STATUS_VALIDATED

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
            cal.add(Calendar.DAY_OF_MONTH, ceil(duration / 2f).toInt() - 1)
            (shortDateFormat().format(startDate)
                    + " → "
                    + shortDateFormat().format(cal.time))
        }
    }

    fun getWageForHoliday(wages: List<Wage>, holidayDate: Date): Wage? {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.time = holidayDate
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        val wagePeriodMin = cal.time
        cal.add(Calendar.MONTH, MAX_ALLOW_HOLIDAY_ON_PAST_MONTH)
        val wagePeriodMax = cal.time
        return wages.lastOrNull { w ->
                !(w.period?.before(wagePeriodMin) ?: true)
                && !(w.period?.after(wagePeriodMax) ?: true)
                && isEditable(w)
        }
    }

    fun findHoliday(wages: List<Wage>, holidayDate: Date): Pair<Wage, Holiday>? {
        val holidayCal = Calendar.getInstance()
        holidayCal.clear()
        holidayCal.time = holidayDate
        val itCal = Calendar.getInstance()
        itCal.clear()
        for (wage in wages) {
            val h = wage.holidays?.firstOrNull {
                if (it.startDate == holidayDate) return@firstOrNull true
                if (it.duration < 3) return@firstOrNull false
                if (it.startDate?.after(holidayDate) != false) return@firstOrNull false
                itCal.time = it.startDate ?: return@firstOrNull false
                if (itCal.get(Calendar.MONTH) != holidayCal.get(Calendar.MONTH)) return@firstOrNull false
                if (itCal.get(Calendar.YEAR) != holidayCal.get(Calendar.YEAR)) return@firstOrNull false
                itCal.add(Calendar.DAY_OF_MONTH, ceil(it.duration / 2f).toInt())
                return@firstOrNull !itCal.before(holidayCal)
            }
            if (h != null) return Pair(wage, h)
        }
        return null
    }

}