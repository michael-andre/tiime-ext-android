package com.wapplix

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatDialogFragment

import com.cubber.tiime.utils.ArchDialogs

/**
 * Created by mike on 28/09/17.
 */

open class ResultDialogFragment<T> : AppCompatDialogFragment(), ArchDialogs.Emitter<T> {

    fun showForResult(receiver: Fragment, tag: String) {
        setTargetFragment(receiver, 0)
        show(receiver.fragmentManager!!, tag)
    }

    fun showForResult(receiver: FragmentActivity, tag: String) {
        show(receiver.supportFragmentManager, tag)
    }

    protected fun sendResult(result: T) {
        ArchDialogs.sendResult(this, result)
        dismiss()
    }

}
