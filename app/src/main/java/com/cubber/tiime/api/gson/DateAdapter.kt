package com.cubber.tiime.api.gson

import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mike on 19/12/17.
 */
class DateAdapter<T : Date>(
        private val format: SimpleDateFormat,
        private val creator: (Long) -> T
) : TypeAdapter<T>() {

    constructor(pattern: String, creator: (Long) -> T) : this(
            SimpleDateFormat(pattern, Locale.US), creator
    )

    override fun write(writer: JsonWriter, value: T?) {
        when (value) {
            null -> writer.nullValue()
            else -> writer.value(format.format(value))
        }
    }

    override fun read(reader: JsonReader): T? {
        return when (reader.peek()) {
            JsonToken.NULL -> null
            JsonToken.STRING -> creator(format.parse(reader.nextString()).time)
            else -> throw JsonSyntaxException("Invalid date format")
        }
    }

}