package com.wapplix.arch

import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.wapplix.withArguments

/**
 * Created by mike on 11/12/17.
 */
fun Fragment.startActivityForResult(intent: Intent, tag: String, options: Bundle? = null, onResult: (Int, Intent?) -> Unit) {
    startActivityForResult(childFragmentManager, intent, tag, options, onResult)
}

fun FragmentActivity.startActivityForResult(intent: Intent, tag: String, options: Bundle? = null, onResult: (Int, Intent?) -> Unit) {
    startActivityForResult(supportFragmentManager, intent, tag, options, onResult)
}

private fun startActivityForResult(fragmentManager: FragmentManager, intent: Intent, tag: String, options: Bundle? = null, onResult: (Int, Intent?) -> Unit) {
    val frag = ActivityResultFragment().withArguments {
        putParcelable(ActivityResultFragment.ARG_INTENT, intent)
        if (options != null) putParcelable(ActivityResultFragment.ARG_OPTIONS, options)
    }
    fragmentManager.beginTransaction()
            .add(frag, tag)
            .runOnCommit { frag.result.onResult = onResult }
            .commitAllowingStateLoss()
}

class ActivityResultFragment: Fragment() {

    internal val result get() = getViewModel<ResultModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = arguments!!.getParcelable<Intent>(ARG_INTENT)
            val options = arguments!!.getBundle(ARG_OPTIONS)
            startActivityForResult(intent, 0, options)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        runOnStarted {
            result.onResult(resultCode, data)
            fragmentManager!!.beginTransaction().remove(this).commitAllowingStateLoss()
        }
    }

    internal companion object {
        const val ARG_INTENT = "intent"
        const val ARG_OPTIONS = "options"
    }

    class ResultModel : ViewModel() {
        lateinit var onResult: (Int, Intent?) -> Unit
    }

}