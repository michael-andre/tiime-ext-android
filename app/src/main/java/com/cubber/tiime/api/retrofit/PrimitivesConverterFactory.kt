package com.cubber.tiime.api.retrofit

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.*

class PrimitivesConverterFactory(gson: Gson) : Converter.Factory() {

    private val plainTextConverter: Converter<Any, RequestBody>
    private val stringConverter: Converter<Any, String>

    init {
        stringConverter = Converter { value ->
            // Prevent extra quotes with getAsString()
            gson.toJsonTree(value).asString
        }
        plainTextConverter = Converter { value ->
            RequestBody.create(PLAIN_TEXT_TYPE, stringConverter.convert(value))
        }
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {

        // Forward primitive, Date and String to Gson for encoding as multipart fields
        return if (type === Date::class.java || type === String::class.java || type is Class<*> && type.isPrimitive) {
            plainTextConverter
        } else null

    }

    override fun stringConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, String>? {

        // Forward primitive, Date and String to Gson for encoding as multipart fields
        return if (type === Date::class.java || type === String::class.java || type is Class<*> && type.isPrimitive) {
            stringConverter
        } else null

    }

    companion object {
        private val PLAIN_TEXT_TYPE = MediaType.parse("text/plain")
    }

}
