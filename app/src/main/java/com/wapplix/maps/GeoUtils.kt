package com.wapplix.maps

import android.content.Context
import android.content.pm.PackageManager

import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsRoute

/**
 * Created by mike on 05/10/17.
 */

object GeoUtils {

    private var geoContext: GeoApiContext? = null

    fun getGeoApiContext(context: Context): GeoApiContext {
        var geoContext = geoContext
        if (geoContext == null) {
            try {
                val apiKey = context.packageManager
                        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                        .metaData
                        .getString("com.google.android.geo.API_KEY")
                geoContext = GeoApiContext.Builder()
                        .apiKey(apiKey)
                        .build()
                this.geoContext = geoContext
                return geoContext
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalStateException("Failed to get API key", e)
            }
        } else {
            return geoContext
        }
    }

    fun getDistance(route: DirectionsRoute): Double {
        return route.legs?.sumByDouble { it.distance.inMeters / 1000.0 } ?: 0.0
    }

}