package com.wapplix

import android.app.Activity
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View

/**
 * Created by mike on 12/12/17.
 */
fun Fragment.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, view: View = getView()!!) {
    Snackbar.make(view, text, duration).show()
}

fun Fragment.showSnackbar(@StringRes text: Int, duration: Int = Snackbar.LENGTH_SHORT, view: View = getView()!!) {
    Snackbar.make(view, text, duration).show()
}

fun DialogFragment.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, view: View = dialog.window.decorView) {
    Snackbar.make(view, text, duration).show()
}

fun DialogFragment.showSnackbar(@StringRes text: Int, duration: Int = Snackbar.LENGTH_SHORT, view: View = dialog.window.decorView) {
    Snackbar.make(view, text, duration).show()
}

fun Activity.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, view: View = findViewById(android.R.id.content)!!) {
    Snackbar.make(view, text, duration).show()
}

fun Activity.showSnackbar(@StringRes text: Int, duration: Int = Snackbar.LENGTH_SHORT, view: View = findViewById(android.R.id.content)!!) {
    Snackbar.make(view, text, duration).show()
}