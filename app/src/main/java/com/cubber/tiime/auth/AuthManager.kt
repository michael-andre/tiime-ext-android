package com.cubber.tiime.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.WindowManager
import com.cubber.tiime.BuildConfig
import com.cubber.tiime.R
import com.cubber.tiime.api.ApiServiceBuilder
import com.cubber.tiime.model.Login
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by mike on 18/12/17.
 */
object AuthManager {

    private const val LOGIN_PLATFORM = "android"
    private const val PREF_ACTIVE_ACCOUNT_NAME = "active_account_name"
    private const val AUTHTOKEN_TYPE = "api_token"
    private const val ACCOUNT_KEY_LOGIN_DATA = "login_data"

    fun login(context: Context, login: String, password: String): Single<Bundle> {
        val authClient = ApiServiceBuilder.createAuthService()
        return authClient.login(
                login = login,
                password = password,
                platform = LOGIN_PLATFORM,
                resolution = Point().let {
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(it)
                    "${it.x}x${it.y}"
                },
                gcmRegistrationId = null,
                appVersion = BuildConfig.VERSION_NAME,
                platformVersion = Build.VERSION.RELEASE,
                platformModel = Build.MODEL
        ).map { data ->
            addAccount(context, data)
        }.observeOn(AndroidSchedulers.mainThread())
    }

    fun getActiveAccount(context: Context): Account? {
        val am = AccountManager.get(context)
        val type = context.getString(R.string.account_type)
        val accounts = am.getAccountsByType(type)
        return when {
            accounts.isEmpty() -> null
            accounts.size == 1 -> accounts[0]
            else -> {
                val sp = PreferenceManager.getDefaultSharedPreferences(context)
                val name = sp.getString(PREF_ACTIVE_ACCOUNT_NAME, null)
                accounts.forEach { if (it.name == name) return it }
                sp.edit().remove(PREF_ACTIVE_ACCOUNT_NAME).apply()
                accounts[0]
            }
        }
    }

    fun optAddAccount(source: Activity): Boolean {
        val am = AccountManager.get(source)
        val type = source.getString(R.string.account_type)
        return if (am.getAccountsByType(type).isEmpty()) {
            am.addAccount(type, AUTHTOKEN_TYPE, null, null, source, null, null)
            true
        } else {
            false
        }
    }

    fun getAccessToken(context: Context, account: Account): String {
        val am = AccountManager.get(context)
        val token = am.peekAuthToken(account, AUTHTOKEN_TYPE)
        if (token == null) {
            removeAccount(context, account)
        }
        return  token
    }

    fun removeAccount(context: Context, account: Account) {
        val am = AccountManager.get(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            am.removeAccountExplicitly(account)
        } else {
            @Suppress("DEPRECATION")
            am.removeAccount(account, null, null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addAccount(context: Context, login: Login): Bundle {
        val am = AccountManager.get(context)
        val type = context.getString(R.string.account_type)

        var account: Account? = am.getAccountsByType(type).lastOrNull { it.name == login.displayName }
        if (account == null) {
            account = Account(login.displayName, type)
            setActiveAccount(context, account.name)
            am.addAccountExplicitly(account, null, null)
        }
        am.setAuthToken(account, AUTHTOKEN_TYPE, login.token)
        am.setUserData(account, ACCOUNT_KEY_LOGIN_DATA, Gson().toJson(login))

        val result = Bundle()
        result.putString(AccountManager.KEY_ACCOUNT_NAME, login.displayName)
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, type)
        result.putString(AccountManager.KEY_AUTHTOKEN, login.token)
        return result
    }

    private fun setActiveAccount(context: Context, name: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (name != sp.getString(PREF_ACTIVE_ACCOUNT_NAME, null)) {
            sp.edit().putString(PREF_ACTIVE_ACCOUNT_NAME, name).apply()
        }
    }

}