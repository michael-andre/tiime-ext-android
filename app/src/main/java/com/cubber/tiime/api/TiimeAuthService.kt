package com.cubber.tiime.api

import com.cubber.tiime.model.Login
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by mike on 18/12/17.
 */
interface TiimeAuthService {

    @POST("login")
    @FormUrlEncoded
    fun login(
            @Field("login") login: String,
            @Field("password") password: String,
            @Field("platform") platform: String,
            @Field("resolution") resolution: String?,
            @Field("gcmRegistrationId") gcmRegistrationId: String?,
            @Field("appVersion") appVersion: String?,
            @Field("platformVersion") platformVersion: String?,
            @Field("platformModel") platformModel: String?
    ): Single<Login>

}