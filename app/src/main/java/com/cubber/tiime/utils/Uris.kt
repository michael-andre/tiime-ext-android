package com.cubber.tiime.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

/**
 * Created by mike on 27/09/17.
 */

object Uris {

    const val DOCUMENT_MS_WORD = "application/msword"
    const val DOCUMENT_OXML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    const val DOCUMENT_MS_EXCEL = "application/vnd.ms-excel"
    const val DOCUMENT_OXML_SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val DOCUMENT_PDF = "application/pdf"
    const val IMAGE = "image/*"

    val SUPPORTED_TYPES = arrayOf(IMAGE, DOCUMENT_PDF, DOCUMENT_MS_WORD, DOCUMENT_MS_EXCEL, DOCUMENT_OXML_DOCUMENT, DOCUMENT_OXML_SPREADSHEET)

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

    fun checkSupportedType(context: Context, uri: Uri): Boolean? {
        val type = getMimeType(context, uri)
        return if (type != null) SUPPORTED_TYPES.contains(type) || type.startsWith("image/") else null
    }

}

class UnsupportedFileTypeException : IllegalArgumentException()
