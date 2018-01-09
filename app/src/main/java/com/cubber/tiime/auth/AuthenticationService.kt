package com.cubber.tiime.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cubber.tiime.app.auth.Authenticator

/**
 * A IBinder service for authentication.
 */
class AuthenticationService : Service() {

    private var mAuthenticator: Authenticator? = null

    override fun onCreate() {
        mAuthenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAuthenticator!!.getIBinder()
    }

    override fun onDestroy() {
        mAuthenticator = null
    }

}
