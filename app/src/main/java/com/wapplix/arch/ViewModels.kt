package com.wapplix.arch

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Created by mike on 11/12/17.
 */
inline fun <reified VM : ViewModel> Fragment.getViewModel(): VM =
        ViewModelProviders.of(this).get(VM::class.java)

inline fun <reified VM : ViewModel> FragmentActivity.getViewModel(): VM =
        ViewModelProviders.of(this).get(VM::class.java)
