package com.wapplix.arch

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.wapplix.bundleOf
import com.wapplix.withArguments
import kotlinx.android.parcel.Parcelize

fun Fragment.showAlert(
        title: CharSequence? = null,
        titleRes: Int = 0,
        message: CharSequence? = null,
        messageRes: Int = 0,
        positiveButton: CharSequence? = null,
        positiveButtonRes: Int = 0,
        negativeButton: CharSequence? = null,
        negativeButtonRes: Int = 0,
        neutralButton: CharSequence? = null,
        neutralButtonRes: Int = 0,
        cancellable: Boolean = true,
        tag: String,
        onResult: (Int?) -> Unit) {
    val frag = AlertDialogFragment().withArguments {
        putParcelable(AlertDialogFragment.ARG_CONFIG, AlertDialogFragment.Config(title, titleRes, message, messageRes, positiveButton, positiveButtonRes, negativeButton, negativeButtonRes, neutralButton, neutralButtonRes, cancellable))
    }
    frag.show(childFragmentManager, tag, onResult)
}

fun FragmentActivity.showAlert(
        title: CharSequence? = null,
        titleRes: Int = 0,
        message: CharSequence? = null,
        messageRes: Int = 0,
        positiveButton: CharSequence? = null,
        positiveButtonRes: Int = 0,
        negativeButton: CharSequence? = null,
        negativeButtonRes: Int = 0,
        neutralButton: CharSequence? = null,
        neutralButtonRes: Int = 0,
        cancellable: Boolean = true,
        tag: String,
        onResult: (Int?) -> Unit) {
    val frag = AlertDialogFragment().withArguments {
        putParcelable(AlertDialogFragment.ARG_CONFIG, AlertDialogFragment.Config(title, titleRes, message, messageRes, positiveButton, positiveButtonRes, negativeButton, negativeButtonRes, neutralButton, neutralButtonRes, cancellable))
    }
    frag.show(supportFragmentManager, tag, onResult)
}

fun Fragment.showConfirm(
        title: CharSequence? = null,
        titleRes: Int = 0,
        message: CharSequence? = null,
        messageRes: Int = 0,
        positiveButtonRes: Int = android.R.string.ok,
        negativeButtonRes: Int = android.R.string.cancel,
        tag: String,
        onConfirm: () -> Unit) {
    showAlert(
            title = title,
            titleRes = titleRes,
            message = message,
            messageRes = messageRes,
            positiveButtonRes = positiveButtonRes,
            negativeButtonRes = negativeButtonRes,
            tag = tag
    ) {
        if (it == DialogInterface.BUTTON_POSITIVE) onConfirm()
    }
}

fun FragmentActivity.showConfirm(
        title: CharSequence? = null,
        titleRes: Int = 0,
        message: CharSequence? = null,
        messageRes: Int = 0,
        positiveButtonRes: Int = android.R.string.ok,
        negativeButtonRes: Int = android.R.string.cancel,
        tag: String,
        onConfirm: () -> Unit) {
    showAlert(
            title = title,
            titleRes = titleRes,
            message = message,
            messageRes = messageRes,
            positiveButtonRes = positiveButtonRes,
            negativeButtonRes = negativeButtonRes,
            tag = tag
    ) {
        if (it == DialogInterface.BUTTON_POSITIVE) onConfirm()
    }
}

class AlertDialogFragment : AppCompatDialogFragment(), ResultEmitter<Int?> {

    @Parcelize
    data class Config(
            val title: CharSequence? = null,
            val titleRes: Int = 0,
            val message: CharSequence? = null,
            val messageRes: Int = 0,
            val positiveButton: CharSequence? = null,
            val positiveButtonRes: Int = 0,
            val negativeButton: CharSequence? = null,
            val negativeButtonRes: Int = 0,
            val neutralButton: CharSequence? = null,
            val neutralButtonRes: Int = 0,
            val cancellable: Boolean = true
    ) : Parcelable

    internal fun init(config: Config): AlertDialogFragment {
        this.arguments = bundleOf { putParcelable("config", config) }
        return this
    }

    fun show(manager: FragmentManager, tag: String, onButton: (Int?) -> Unit) {
        super.show(manager.beginTransaction().runOnCommit { result.onResult = onButton }, tag)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val config = arguments!!.getParcelable<Config>("config")
        return AlertDialog.Builder(context!!)
                .apply {
                    val handler: (DialogInterface, Int) -> Unit = { _, i -> result.onResult(i) }
                    if (config.title != null) setTitle(config.title)
                    if (config.titleRes != 0) setTitle(config.titleRes)
                    if (config.message != null) setMessage(config.message)
                    if (config.messageRes != 0) setMessage(config.messageRes)
                    if (config.positiveButton != null) setPositiveButton(config.positiveButton, handler)
                    if (config.positiveButtonRes != 0) setPositiveButton(config.positiveButtonRes, handler)
                    if (config.negativeButton != null) setNegativeButton(config.negativeButton, handler)
                    if (config.negativeButtonRes != 0) setNegativeButton(config.negativeButtonRes, handler)
                    if (config.neutralButton != null) setNeutralButton(config.neutralButton, handler)
                    if (config.neutralButtonRes != 0) setNeutralButton(config.neutralButtonRes, handler)
                    setCancelable(config.cancellable)
                }
                .create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        result.onResult(null)
    }

    companion object {
        const val ARG_CONFIG = "config"
    }

}