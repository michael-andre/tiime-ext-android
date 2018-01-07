package com.cubber.tiime.api.retrofit

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.IOException

/**
 * A request body adapter to upload a file from a content provider
 */
class UriContentBody(private val contentResolver: ContentResolver, private val uri: Uri) : RequestBody() {

    var filename: String? = null
        private set
    private var contentLength: Long = 0

    init {
        val projection = arrayOf(OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME)
        val infoCursor = contentResolver.query(uri, projection, null, null, null)
        if (infoCursor != null) {
            infoCursor.use {
                val sizeIndex = infoCursor.getColumnIndex(OpenableColumns.SIZE)
                val nameIndex = infoCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                infoCursor.moveToFirst()
                filename = infoCursor.getString(nameIndex)
                contentLength = if (!infoCursor.isNull(sizeIndex)) infoCursor.getLong(sizeIndex) else -1
            }
        } else {
            val f = File(uri.path)
            filename = f.name
            contentLength = f.length()
        }
        if (contentLength == 0L) contentLength = -1
    }

    override fun contentType(): MediaType? {
        var type = contentResolver.getType(uri)
        if (type == null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))
        }

        return MediaType.parse(type!!)
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        if (contentLength == -1L) {
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                contentLength = pfd.statSize
                pfd.close()
            }
        }
        return contentLength
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream != null) sink.writeAll(Okio.source(inputStream))
    }

}
