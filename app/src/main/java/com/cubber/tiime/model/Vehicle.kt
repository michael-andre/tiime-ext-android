package com.cubber.tiime.model

import android.net.Uri
import android.os.Parcelable
import android.support.annotation.StringDef
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by mike on 25/09/17.
 */
@Parcelize
data class Vehicle(
        var id: Long = 0,
        var name: String? = null,
        @Type var type: String? = null,
        @FiscalPower var fiscalPower: String? = null,
        var card: Uri? = null,
        var deletionDate: Date? = null
) : Parcelable {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_CAR, TYPE_TWO_WHEELER_1, TYPE_TWO_WHEELER_2)
    annotation class Type

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(FISCAL_POWER_1_2, FISCAL_POWER_3_4_5, FISCAL_POWER_6_OR_MORE, FISCAL_POWER_3_OR_LESS, FISCAL_POWER_4, FISCAL_POWER_5, FISCAL_POWER_6, FISCAL_POWER_7_OR_MORE)
    annotation class FiscalPower

    companion object {

        const val TYPE_CAR = "A"
        const val TYPE_TWO_WHEELER_1 = "C"
        const val TYPE_TWO_WHEELER_2 = "M"

        const val FISCAL_POWER_1_2 = "1CV"
        const val FISCAL_POWER_3_4_5 = "3CV"
        const val FISCAL_POWER_6_OR_MORE = "6CV"
        const val FISCAL_POWER_3_OR_LESS = "3CV"
        const val FISCAL_POWER_4 = "4CV"
        const val FISCAL_POWER_5 = "5CV"
        const val FISCAL_POWER_6 = "6CV"
        const val FISCAL_POWER_7_OR_MORE = "7CV"

    }

}