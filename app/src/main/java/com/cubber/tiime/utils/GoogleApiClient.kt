package com.cubber.tiime.utils

import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient

/**
 * Created by mike on 12/12/17.
 */
fun GoogleApiClient.connect(onConnected: (GoogleApiClient) -> Unit) {
    registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {

        override fun onConnected(p0: Bundle?) {
            onConnected(this@connect)
        }

        override fun onConnectionSuspended(p0: Int) { }

    })
    connect()
}