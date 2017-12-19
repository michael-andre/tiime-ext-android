package com.cubber.tiime.app.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A IBinder service for authentication.
 */
class AuthenticationService : Service() {

    private var authenticator: Authenticator? = null

    override fun onCreate() {
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator?.iBinder
    }

    override fun onDestroy() {
        authenticator = null
    }

}
