package com.cubber.tiime.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.cubber.tiime.R
import com.wapplix.showSnackbar
import java.net.UnknownHostException

/**
 * Created by mike on 12/12/17.
 */
fun Fragment.showErrorSnackbar(ex: Throwable?, @StringRes message: Int? = null) {
    showSnackbar(getErrorText(context!!, ex, message))
}

fun FragmentActivity.showErrorSnackbar(ex: Throwable?, @StringRes message: Int? = null) {
    showSnackbar(getErrorText(this, ex, message))
}

private fun getErrorText(context: Context, ex: Throwable?, @StringRes message: Int?): CharSequence {
    var text = when (ex) {
        is UnknownHostException -> context.getString(R.string.network_error)
        else -> context.getString(R.string.generic_error_message)
    }
    if (message != null) text = context.getString(message) + " " + text
    return text
}