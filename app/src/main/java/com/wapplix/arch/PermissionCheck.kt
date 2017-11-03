package com.wapplix.arch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import com.google.common.primitives.Ints

/**
 * Created by mike on 03/10/17.
 */

class PermissionCheck(context: Context, private vararg val permissions: String) : LiveData<IntArray>() {

    private val context: Context = context.applicationContext
    private val requestSignal = EventData<Any>()

    fun handleOn(activity: FragmentActivity, tag: String) {
        requestSignal.observe(activity, Observer {
            val f = RequestPermissionsFragment()
            f.permissions = permissions
            f.permissionCheck = this
            activity.supportFragmentManager.beginTransaction().add(f, tag).commit()
        })
    }

    fun granted(): LiveData<Boolean> {
        return Transformations.map(this
        ) { results -> results.all { r -> r == PackageManager.PERMISSION_GRANTED } }
    }

    override fun onActive() {
        super.onActive()
        val results = permissions.map { p -> ContextCompat.checkSelfPermission(context, p) }
        if (results.all { r -> r == PackageManager.PERMISSION_GRANTED }) {
            postValue(Ints.toArray(results))
        } else {
            requestSignal.trigger()
        }
    }

    class RequestPermissionsFragment : Fragment() {

        var permissions: Array<out String>? = null
        var permissionCheck: PermissionCheck? = null

        init {
            retainInstance = true
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState == null) {
                requestPermissions(permissions!!, REQUEST_PERMISSIONS)
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            if (requestCode == REQUEST_PERMISSIONS) {
                permissionCheck!!.postValue(grantResults)
                fragmentManager!!.beginTransaction().remove(this).commitAllowingStateLoss()
            }
        }

        companion object {

            private val REQUEST_PERMISSIONS = 1
        }

    }

}