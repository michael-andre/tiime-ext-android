package com.cubber.tiime.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Created by mike on 19/12/17.
 */
data class MileageAllowanceRequest(
        var vehicleId: Long = 0,
        var fromAddress: String? = null,
        var toAddress: String? = null,
        var purpose: String? = null,
        var distance: Int? = null,
        var tripDates: Set<Date>? = null,
        var comment: String? = null,
        var roundTrip: Boolean? = null,
        var polyline: List<LatLng>? = null
)