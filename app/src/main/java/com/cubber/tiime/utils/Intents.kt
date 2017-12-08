package com.cubber.tiime.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.widget.Toast

import com.cubber.tiime.R

/**
 * Created by mike on 26/09/17.
 */

object Intents {

    fun getContent(allowedTypes: Array<String>? = null, chooserTitle: CharSequence? = null): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        when {
            allowedTypes?.isEmpty() != false -> intent.type = "*/*"
            allowedTypes.size == 1 -> intent.type = allowedTypes[0]
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_MIME_TYPES, allowedTypes)
            }
            else -> intent.type = TextUtils.join(",", allowedTypes)
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return Intent.createChooser(intent, chooserTitle)
    }

    fun view(context: Context, uri: Uri): Intent? {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (!checkIntent(context, intent)) return null
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    fun startViewActivity(context: Context, uri: Uri) {
        val intent = view(context, uri)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.no_app_available, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIntent(context: Context, intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0
    }

}
