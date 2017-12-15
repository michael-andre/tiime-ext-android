package com.wapplix.arch

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.wapplix.withArguments

/**
 * Created by mike on 11/12/17.
 */
fun Fragment.requestPermissions(permissions: Array<out String>, tag: String, onResult: (IntArray) -> Unit) {
    requestPermissions(childFragmentManager, permissions, tag, onResult)
}

fun FragmentActivity.requestPermissions(permissions: Array<out String>, tag: String, onResult: (IntArray) -> Unit) {
    requestPermissions(supportFragmentManager, permissions, tag, onResult)
}

private fun requestPermissions(fragmentManager: FragmentManager, permissions: Array<out String>, tag: String, onResult: (IntArray) -> Unit) {
    val frag = PermissionsRequestFragment().withArguments {
        putStringArray(PermissionsRequestFragment.ARG_PERMISSIONS, permissions)
    }
    fragmentManager.beginTransaction()
            .add(frag, tag)
            .runOnCommit { frag.result.onResult = onResult }
            .commitAllowingStateLoss()
}

fun <T : LifecycleOwner> UiModel<T>.requirePermissions(permissions: Array<out String>, tag: String, onGranted: () -> Unit) {
    val results = permissions.map { p -> android.support.v4.content.ContextCompat.checkSelfPermission(getApplication(), p) }.toIntArray()
    val check: (IntArray) -> Boolean = { it.all { r -> r == android.content.pm.PackageManager.PERMISSION_GRANTED } }
    if (check(results)) {
        onGranted()
    } else {
        onUi {
            if (this is Fragment) requestPermissions(permissions, tag) { r -> if (check(r)) onGranted() }
            else if (this is FragmentActivity) requestPermissions(permissions, tag) { r -> if (check(r)) onGranted() }
        }
    }
}

class PermissionsRequestFragment: Fragment() {

    internal val result get() = getViewModel<ResultModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val permissions = arguments!!.getStringArray(ARG_PERMISSIONS)
            requestPermissions(permissions, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        runOnStarted {
            result.onResult(grantResults)
            fragmentManager!!.beginTransaction().remove(this).commitAllowingStateLoss()
        }
    }

    internal companion object {
        const val ARG_PERMISSIONS = "permissions"
    }

    class ResultModel : ViewModel() {
        lateinit var onResult: (IntArray) -> Unit
    }

}