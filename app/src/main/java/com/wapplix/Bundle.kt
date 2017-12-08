package com.wapplix

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by mike on 04/12/17.
 */
inline fun bundleOf(init: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    bundle.init()
    return bundle
}

inline fun <T : Fragment> T.withArguments(apply: Bundle.() -> Unit): T {
    var args = arguments
    if (args == null) {
        args = Bundle()
        arguments = args
    }
    args.apply()
    return this
}