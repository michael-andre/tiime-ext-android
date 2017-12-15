package com.cubber.tiime.model

import android.os.Parcelable
import android.support.annotation.StringDef
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

@Parcelize
data class Holiday(
    var id: Long = 0,
    var startDate: Date? = null,
    @Type var type: String? = null,
    var duration: Int = 0
) : Parcelable {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_COMPENSATORY_TIME, TYPE_PAID_VACATION, TYPE_SICK_LEAVE, TYPE_WORK_ACCIDENT, TYPE_FAMILY_MATTERS, TYPE_UNPAID_HOLIDAY)
    annotation class Type

    companion object {

        const val TYPE_COMPENSATORY_TIME = "compensatoryTime"
        const val TYPE_PAID_VACATION = "paidVacation"
        const val TYPE_SICK_LEAVE = "sickLeave"
        const val TYPE_WORK_ACCIDENT = "workAccident"
        const val TYPE_FAMILY_MATTERS = "familyMatters"
        const val TYPE_UNPAID_HOLIDAY = "unpaidHoliday"

        @Type
        val TYPES = arrayOf(TYPE_COMPENSATORY_TIME, TYPE_PAID_VACATION, TYPE_SICK_LEAVE, TYPE_WORK_ACCIDENT, TYPE_FAMILY_MATTERS, TYPE_UNPAID_HOLIDAY)
    }

}
