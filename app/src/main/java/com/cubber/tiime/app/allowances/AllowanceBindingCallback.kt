package com.cubber.tiime.app.allowances

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

    private var mPolyline: Polyline? = null
    private var mStartMarker: Marker? = null
    private var mEndMarker: Marker? = null

    private var mMarkerDescriptor: BitmapDescriptor? = null
    private var mPolylineWidth: Float = 0.toFloat()
    private var mPolylineColor: Int = 0

    fun setPolyline(mapView: MapView, polyline: EncodedPolyline?) {

        if (mMarkerDescriptor == null) {
            mMarkerDescriptor = BitmapDescriptorFactory.fromBitmap(Bitmaps.fromDrawable(
                    ContextCompat.getDrawable(mapView.context, R.drawable.trip_marker)
            ))
            mPolylineWidth = mapView.resources.getDimension(R.dimen.trip_polyline_width)
            mPolylineColor = ContextCompat.getColor(mapView.context, R.color.trip_polyline)
        }
        val polylineOpt = if (polyline != null)
            PolylineOptions()
                    .addAll(polyline.decodePath().map { p -> LatLng(p.lat, p.lng) })
                    .width(mPolylineWidth)
                    .color(mPolylineColor)
        else
            null
        val startOpt = if (polylineOpt != null)
            MarkerOptions()
                    .icon(mMarkerDescriptor)
                    .position(polylineOpt.points[0])
                    .anchor(0.5f, 0.5f)
        else
            null
        val endOpt = if (polylineOpt != null)
            MarkerOptions()
                    .icon(mMarkerDescriptor)
                    .position(polylineOpt.points[polylineOpt.points.size - 1])
                    .anchor(0.5f, 0.5f)
        else
            null
        mapView.getMapAsync { map ->
            if (mPolyline != null) mPolyline!!.remove()
            if (mStartMarker != null) mStartMarker!!.remove()
            if (mEndMarker != null) mEndMarker!!.remove()
            if (polylineOpt != null) {
                mPolyline = map.addPolyline(polylineOpt)
                mStartMarker = map.addMarker(startOpt)
                mEndMarker = map.addMarker(endOpt)
                var bounds = LatLngBounds(mPolyline!!.points[0], mPolyline!!.points[0])
                for (p in mPolyline!!.points) bounds = bounds.including(p)
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.resources.getDimensionPixelSize(R.dimen.spacing)))
            } else {
                mPolyline = null
                mStartMarker = null
                mEndMarker = null
            }
        }
    }

}
