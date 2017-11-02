package com.cubber.tiime.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

/**
 * Created by mike on 27/09/17.
 */

object Uris {

    fun getMimeType(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> context.contentResolver.getType(uri)
            ContentResolver.SCHEME_FILE -> {
                val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                if (ext == null)
                    null
                else
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
            }
            else -> null
        }
    }

}
