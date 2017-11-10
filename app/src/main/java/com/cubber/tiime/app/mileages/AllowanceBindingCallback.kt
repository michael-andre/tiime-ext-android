package com.cubber.tiime.app.mileages

import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.support.v4.content.ContextCompat
import com.cubber.tiime.R
import com.cubber.tiime.utils.Bitmaps
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.maps.model.EncodedPolyline

/**
 * Created by mike on 05/10/17.
 */

open class AllowanceBindingCallback<T : ViewDataBinding> : OnRebindCallback<T>() {

    private var polyline: Polyline? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    private var markerDescriptor: BitmapDescriptor? = null
    private var polylineWidth: Float = 0f
    private var polylineColor: Int = 0

    fun setPolyline(mapView: MapView, encodedPolyline: EncodedPolyline?) {

        if (markerDescriptor == null) {
            markerDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmaps.fromDrawable(
                    ContextCompat.getDrawable(mapView.context, R.drawable.trip_marker)
            ))
            polylineWidth = mapView.resources.getDimension(R.dimen.trip_polyline_width)
            polylineColor = ContextCompat.getColor(mapView.context, R.color.trip_polyline)
        }
        val polylineOpt = if (encodedPolyline != null)
            PolylineOptions()
                    .addAll(encodedPolyline.decodePath().map { p -> LatLng(p.lat, p.lng) })
                    .width(polylineWidth)
                    .color(polylineColor)
        else
            null
        val startOpt = if (polylineOpt != null)
            MarkerOptions()
                    .icon(markerDescriptor)
                    .position(polylineOpt.points[0])
                    .anchor(0.5f, 0.5f)
        else
            null
        val endOpt = if (polylineOpt != null)
            MarkerOptions()
                    .icon(markerDescriptor)
                    .position(polylineOpt.points[polylineOpt.points.size - 1])
                    .anchor(0.5f, 0.5f)
        else
            null
        mapView.getMapAsync { map ->
            polyline?.apply { remove() }
            startMarker?.apply { remove() }
            endMarker?.apply { remove() }
            if (polylineOpt != null) {
                polyline = map.addPolyline(polylineOpt)
                startMarker = map.addMarker(startOpt)
                endMarker = map.addMarker(endOpt)
                val bounds = LatLngBounds.Builder()
                        .apply { polylineOpt.points.forEach { include(it) } }
                        .build()
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.resources.getDimensionPixelSize(R.dimen.spacing)))
            } else {
                polyline = null
                startMarker = null
                endMarker = null
            }
        }
    }

}
