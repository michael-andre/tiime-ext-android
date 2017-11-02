package com.cubber.tiime.model

import android.net.Uri
import android.support.annotation.StringDef

/**
 * Created by mike on 25/09/17.
 */
data class Vehicle(
    var id: Long = 0,
    var name: String? = null,
    @Type var type: String? = null,
    @FiscalPower var fiscal_power: String? = null,
    var card_uri: Uri? = null
) {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_CAR, TYPE_TWO_WHEELER_1, TYPE_TWO_WHEELER_2)
    annotation class Type

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(FISCAL_POWER_1_2, FISCAL_POWER_3_4_5, FISCAL_POWER_6_OR_MORE, FISCAL_POWER_3_OR_LESS, FISCAL_POWER_4, FISCAL_POWER_5, FISCAL_POWER_6, FISCAL_POWER_7_OR_MORE)
    annotation class FiscalPower

    companion object {
        const val TYPE_CAR = "car"
        const val TYPE_TWO_WHEELER_1 = "twoWheeler1"
        const val TYPE_TWO_WHEELER_2 = "twoWheeler2"
        const val FISCAL_POWER_1_2 = "1-2"
        const val FISCAL_POWER_3_4_5 = "3-5"
        const val FISCAL_POWER_6_OR_MORE = "6+"
        const val FISCAL_POWER_3_OR_LESS = "3-"
        const val FISCAL_POWER_4 = "4"
        const val FISCAL_POWER_5 = "5"
        const val FISCAL_POWER_6 = "6"
        const val FISCAL_POWER_7_OR_MORE = "7+"
    }

}