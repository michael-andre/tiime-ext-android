package com.cubber.tiime.model

import com.google.maps.model.EncodedPolyline
import java.util.*

/**
 * Created by mike on 21/09/17.
 */

data class MileageAllowance(
        var id: Long = 0,
        var vehicleId: Long = 0,
        var fromAddress: String? = null,
        var toAddress: String? = null,
        var purpose: String? = null,
        var distance: Int? = null,
        var dates: Set<Date>? = null,
        var comment: String? = null,
        var roundTrip: Boolean? = null,
        var polyline: EncodedPolyline? = null
)