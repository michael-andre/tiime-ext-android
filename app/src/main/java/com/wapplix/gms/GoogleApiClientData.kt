package com.wapplix.gms

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

/**
 * Created by mike on 29/09/17.
 */

class GoogleApiClientData(
        context: Context,
        builder: GoogleApiClient.Builder.() -> Unit
) : LiveData<GoogleApiClient>(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var client = with(GoogleApiClient.Builder(context, this, this)) {
        builder(this)
        build()
    }

    override fun onActive() {
        client.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        value = client
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        value = null
    }

}
