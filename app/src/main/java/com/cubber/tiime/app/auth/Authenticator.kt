package com.cubber.tiime.app.auth

import android.accounts.*
import android.content.Context
import android.os.Bundle
import com.cubber.tiime.R

/**
 * Authenticator service for user account
 */
class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {

    private val context = context.applicationContext

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String?, requiredFeatures: Array<String>?, options: Bundle): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, LoginActivity.newIntent(context, response))
        return bundle
    }


    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        return context.getString(R.string.app_name)
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle {
        throw UnsupportedOperationException()
    }

}
