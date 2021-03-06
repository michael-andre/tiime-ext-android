package com.cubber.tiime.model

import com.google.android.gms.maps.model.LatLng
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
        var tripDate: Date? = null,
        var comment: String? = null,
        var roundTrip2: Boolean? = null,
        var polyline: List<LatLng>? = null
)