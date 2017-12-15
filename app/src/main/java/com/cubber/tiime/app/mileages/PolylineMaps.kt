package com.cubber.tiime.app.mileages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.cubber.tiime.R
import com.cubber.tiime.utils.Bitmaps
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

/**
 * Created by mike on 05/10/17.
 */

open class PolylineMapHelper(private val mapView: MapView) {

    private var polyline: Polyline? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    private var map: GoogleMap? = null

    fun applyOptions(options: PolylineMapOptions?) {
        if (options == null) mapView.visibility = View.INVISIBLE
        map?.let {
            polyline?.remove()
            startMarker?.remove()
            endMarker?.remove()
            if (options != null) {
                it.moveCamera(options.cameraUpdate)
                polyline = it.addPolyline(options.polylineOpt)
                startMarker = it.addMarker(options.startOpt)
                endMarker = it.addMarker(options.endOpt)
                mapView.visibility = View.VISIBLE
            } else {
                polyline = null
                startMarker = null
                endMarker = null
            }
        } ?: mapView.getMapAsync {
            map = it
            applyOptions(options)
        }
    }

}

class PolylineMapOptionsFactory(context: Context) {

    private val markerDescriptor: BitmapDescriptor
    private val polylineWidth = context.resources.getDimension(R.dimen.trip_polyline_width)
    private val polylineColor = ContextCompat.getColor(context, R.color.trip_polyline)
    private val boundsPadding = context.resources.getDimensionPixelSize(R.dimen.spacing)

    init {
        MapsInitializer.initialize(context)
        markerDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmaps.fromDrawable(
                ContextCompat.getDrawable(context, R.drawable.trip_marker)
        ))
    }

    fun create(coordinates: List<LatLng>) = PolylineMapOptions(
            startOpt = MarkerOptions()
                    .icon(markerDescriptor)
                    .position(coordinates.first())
                    .anchor(0.5f, 0.5f),
            endOpt = MarkerOptions()
                    .icon(markerDescriptor)
                    .position(coordinates.last())
                    .anchor(0.5f, 0.5f),
            polylineOpt = PolylineOptions()
                    .addAll(coordinates)
                    .width(polylineWidth)
                    .color(polylineColor),
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder()
                            .apply { coordinates.forEach { include(it) } }
                            .build(),
                    boundsPadding
            )
    )

}

data class PolylineMapOptions(
        val startOpt: MarkerOptions,
        val endOpt: MarkerOptions,
        val polylineOpt: PolylineOptions,
        val cameraUpdate: CameraUpdate
)
