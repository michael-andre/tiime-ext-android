package com.cubber.tiime.api

import android.os.Build
import com.cubber.tiime.BuildConfig
import com.cubber.tiime.api.gson.DateAdapter
import com.cubber.tiime.api.retrofit.PrimitivesConverterFactory
import com.cubber.tiime.model.ApiError
import com.cubber.tiime.utils.Month
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by mike on 18/12/17.
 */
object ApiServiceBuilder {

    private const val API_BASE = "https://api.tiime.fr/v2/"
    private const val API_DATE_FORMAT = "yyyy-MM-dd"
    private const val API_MONTH_FORMAT = "yyyy-MM"

    fun createAuthService() = createService(TiimeAuthService::class.java)
    fun createDataService(accessToken: String) = createService(TiimeDataService::class.java, accessToken)

    private fun <T> createService(type: Class<T>, accessToken: String? = null): T {
        val gson = GsonBuilder()
                .setDateFormat(API_DATE_FORMAT)
                .registerTypeAdapter(Month::class.java, DateAdapter(API_MONTH_FORMAT) { Month(it) })
                .create()
        val client = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val builder = chain.request().newBuilder()
                    addApiHeaders(builder, accessToken)
                    builder.header("Accept", "application/json")
                    chain.proceed(builder.build())
                }
                .addInterceptor { chain ->
                    val response = chain.proceed(chain.request())
                    if (!response.isSuccessful) {
                        try {
                            val error = gson.fromJson(response.body()?.charStream(), ApiError::class.java)
                            throw ApiException(response.code(), error)
                        } catch (e: JsonSyntaxException) {
                            throw ApiException(response.code(), null)
                        }
                    }
                    response
                }
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(PrimitivesConverterFactory(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(type)
    }

    private fun addApiHeaders(builder: Request.Builder, accessToken: String?) {
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken)
        }
        builder.header("User-Agent", BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")")
        builder.header("App-Version", BuildConfig.VERSION_NAME)
        if (BuildConfig.DEBUG) builder.header("App-Debug", "true")
        builder.header("Accept-Language", Locale.getDefault().language)
    }

}