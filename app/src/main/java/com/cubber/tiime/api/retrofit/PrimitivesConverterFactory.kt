package com.cubber.tiime.api.retrofit

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class PrimitivesConverterFactory(gson: Gson) : Converter.Factory() {

    private val stringConverter: Converter<Any, String> = Converter { value ->
        // Prevent extra quotes with getAsString()
        gson.toJsonTree(value).asString
    }

    private val stringIterableConverter: Converter<in Iterable<*>?, String> = Converter { value ->
        // Prevent extra quotes with getAsString()
        value?.joinToString(",") { v -> gson.toJsonTree(v).asString }
    }

    private val plainTextConverter: Converter<Any, RequestBody> = Converter { value ->
        RequestBody.create(PLAIN_TEXT_TYPE, stringConverter.convert(value))
    }

    private val plainTextIterableConverter: Converter<in Iterable<*>, RequestBody> = Converter { value ->
        RequestBody.create(PLAIN_TEXT_TYPE, stringIterableConverter.convert(value))
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {

        // Forward primitive, Date and String to Gson for encoding as multipart fields
        return if (type === Date::class.java || type === String::class.java || type is Class<*> && type.isPrimitive) {
            plainTextConverter
        } else if (parameterAnnotations?.any { it is CommaSeparated } == true && type is ParameterizedType && Iterable::class.java.isAssignableFrom(type as Class<*>)) {
            plainTextIterableConverter
        } else {
            null
        }

    }

    override fun stringConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, String>? {

        // Forward primitive, Date and String to Gson for encoding as multipart fields
        return if (type is Class<*> && Date::class.java.isAssignableFrom(type) || type === String::class.java || type is Class<*> && type.isPrimitive) {
            stringConverter
        } else if (annotations?.any { it is CommaSeparated } == true && type is ParameterizedType && Iterable::class.java.isAssignableFrom(type as Class<*>)) {
            stringIterableConverter
        } else {
            null
        }

    }

    companion object {
        private val PLAIN_TEXT_TYPE = MediaType.parse("text/plain")
    }

}
