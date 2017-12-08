package com.wapplix

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment

/**
 * Created by mike on 06/12/17.
 */
class ProgressDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ProgressDialog(context)
        dialog.isIndeterminate = true
        return dialog
    }

}