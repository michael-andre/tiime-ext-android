package com.wapplix.maps

import android.content.Context

import com.google.maps.DirectionsApi
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode

/**
 * Created by mike on 04/10/17.
 */

class DirectionsData(context: Context, origin: String, destination: String) : PendingResultData<DirectionsResult>(DirectionsApi.getDirections(GeoUtils.getGeoApiContext(context), origin, destination)
        .mode(TravelMode.DRIVING)
        .alternatives(false))
