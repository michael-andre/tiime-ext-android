package com.cubber.tiime.model

/**
 * Created by mike on 26/09/17.
 */

data class Associate(
        var id: Long = 0,
        var name: String? = null,
        var defaultFromAddress: String? = null,
        var defaultVehicleId: Long? = null,
        var vehicles: List<Vehicle>
)