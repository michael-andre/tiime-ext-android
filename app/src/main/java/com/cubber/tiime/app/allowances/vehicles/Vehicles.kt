package com.cubber.tiime.app.allowances.vehicles

import com.cubber.tiime.model.Vehicle

import java.util.Arrays

/**
 * Created by mike on 11/10/17.
 */

object Vehicles {

    fun getAvailablePowers(@Vehicle.Type type: String): List<String>? {
        when (type) {
            Vehicle.TYPE_CAR -> return Arrays.asList(
                    Vehicle.FISCAL_POWER_3_OR_LESS,
                    Vehicle.FISCAL_POWER_4,
                    Vehicle.FISCAL_POWER_5,
                    Vehicle.FISCAL_POWER_6,
                    Vehicle.FISCAL_POWER_7_OR_MORE
            )
            Vehicle.TYPE_TWO_WHEELER_2 -> return Arrays.asList(
                    Vehicle.FISCAL_POWER_1_2,
                    Vehicle.FISCAL_POWER_3_4_5,
                    Vehicle.FISCAL_POWER_6_OR_MORE
            )
        }
        return null
    }

}
