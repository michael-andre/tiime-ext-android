package com.cubber.tiime.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.cubber.tiime.model.MileageAllowance
import com.cubber.tiime.utils.setCallback
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.wapplix.maps.GeoUtils

/**
 * Created by mike on 14/12/17.
 */
class PolylineService(private val geoApiContext: GeoApiContext) {

    val loadingPolylines: LiveData<out Set<Long>> get() = loadingPolylinesMut
    private val loadingPolylinesMut = MutableLiveData<MutableSet<Long>>()

    init {
        loadingPolylinesMut.value = mutableSetOf()
    }

    fun loadPolyline(allowance: MileageAllowance) {
        if (loadingPolylines.value?.contains(allowance.id) == true) return
        if (allowance.fromAddress.isNullOrBlank() || allowance.toAddress.isNullOrBlank()) return
        loadingPolylinesMut.postValue(loadingPolylinesMut.value?.apply { add(allowance.id) })
        DirectionsApi.getDirections(geoApiContext, allowance.fromAddress, allowance.toAddress)
                .mode(TravelMode.DRIVING)
                .alternatives(false)
                .setCallback(
                        onResult = { r ->
                            allowance.polyline = r.routes[0].overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }
                            loadingPolylinesMut.postValue(loadingPolylinesMut.value?.apply { remove(allowance.id) })
                        },
                        onFailure = { e ->
                            Log.w("PolylineService", "Failed to get directions", e)
                            allowance.polyline = emptyList()
                            loadingPolylinesMut.postValue(loadingPolylinesMut.value?.apply { remove(allowance.id) })
                        }
                )
    }

    companion object {

        private var polylineService: PolylineService? = null

        fun getInstance(context: Context): PolylineService
            = polylineService ?: run {
                val service = PolylineService(GeoUtils.getGeoApiContext(context))
                polylineService = service
                service
            }

    }

}