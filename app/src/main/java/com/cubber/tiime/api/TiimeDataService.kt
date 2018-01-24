package com.cubber.tiime.api

import com.cubber.tiime.api.retrofit.CommaSeparated
import com.cubber.tiime.model.*
import com.cubber.tiime.utils.Month
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*

/**
 * Created by mike on 05/12/17.
 */
interface TiimeDataService {

    // Associates

    @GET("associates/me?activeVehicles=true")
    fun getAssociate(): Single<Associate>

    // Mileages

    @GET("associates/me/mileages?active=true")
    fun getAssociateMileages(
            @Query("offset") offset: Int? = null,
            @Query("limit") limit: Int? = null
    ): Single<MileageAllowancesList>

    /*@POST("associates/me/mileages")
    fun addAssociateMileages(
            @Body mileageAllowance: MileageAllowanceRequest
    ): Single<List<MileageAllowance>>*/

    @POST("associates/me/mileages")
    @FormUrlEncoded
    fun addAssociateMileages(
            @Field("vehicleId") vehicleId: Long,
            @Field("purpose") purpose: String,
            @Field("distance") distance: Int,
            @Field("dates") @CommaSeparated dates: Set<@JvmSuppressWildcards Date>,
            @Field("fromAddress") fromAddress: String? = null,
            @Field("toAddress") toAddress: String? = null,
            @Field("comment") comment: String? = null,
            @Field("roundTrip") roundTrip: Boolean? = null,
            @Field("polyline") polyline: List<LatLng>? = null
    ): Single<List<MileageAllowance>>

    @DELETE("associates/me/mileages/{id}")
    fun deleteAssociateMileage(
            @Path("id") id: Long
    ): Completable

    // Vehicles

    /*@POST("associates/me/vehicles")
    fun addAssociateVehicle(
            @Body vehicle: Vehicle
    ): Single<Vehicle>*/

    @POST("associates/me/vehicles")
    @Multipart
    fun addAssociateVehicle(
            @Part("name") name: String,
            @Part("type") @Vehicle.Type type: String,
            @Part("fiscalPower") @Vehicle.FiscalPower fiscalPower: String,
            @Part("card") card: RequestBody? = null
    ): Single<Vehicle>

    /*@PATCH("associates/me/vehicles/{id}")
    fun updateAssociateVehicle(
            @Path("id") id: Long,
            @Body vehicle: Vehicle
    ): Single<Vehicle>*/

    @PATCH("associates/me/vehicles/{id}")
    @Multipart
    fun updateAssociateVehicle(
            @Path("id") id: Long,
            @Part("name") name: String? = null,
            @Part("card") card: RequestBody? = null
    ): Single<Vehicle>

    @DELETE("associates/me/vehicles/{id}")
    fun deleteAssociateVehicle(
            @Path("id") id: Long
    ): Completable

    // Clients

    @GET("clients")
    fun getClients(): Single<ClientsList>

    // Employees

    @GET("employees")
    fun getEmployees(): Single<EmployeesList>

    // Wages

    @GET("employees/{id}/wages")
    fun getEmployeeWages(
            @Path("id") id: Long,
            @Query("from") from: Month? = null,
            @Query("to") to: Month? = null
    ): Single<WagesList>

    @GET("employees/{employeeId}/wages/{id}")
    fun getEmployeeWage(
            @Path("employeeId") employeeId: Long,
            @Path("id") id: Long
    ): Single<Wage>

    @POST("employees/{employeeId}/wages/{id}")
    @Multipart
    fun updateEmployeeWage(
            @Path("employeeId") employeeId: Long,
            @Path("id") id: Long,
            @Part("status") @Wage.Status status: String? = null,
            @Part("increase") increase: BigDecimal? = null,
            @Part("increaseType") @Wage.SalaryType increaseType: String? = null,
            @Part("bonus") bonus: BigDecimal? = null,
            @Part("bonusType") @Wage.SalaryType bonusType: String? = null,
            @Part("comment") comment: String? = null,
            @Part("attachment") attachment: RequestBody? = null
    ): Single<Wage>

    /*@POST("employees/{employeeId}/wages/{wageId}")
    fun addEmployeeWageHoliday(
            @Path("employeeId") employeeId: Long,
            @Path("wageId") wageId: Long,
            @Body holiday: Holiday
    ): Single<Holiday>*/

    @POST("employees/{employeeId}/wages/{wageId}/holidays")
    @FormUrlEncoded
    fun addEmployeeWageHoliday(
            @Path("employeeId") employeeId: Long,
            @Path("wageId") wageId: Long,
            @Field("startDate") startDate: Date,
            @Field("type") @Holiday.Type type: String,
            @Field("duration") duration: Int
    ): Single<Holiday>

    @DELETE("employees/{employeeId}/wages/{wageId}/holidays/{id}")
    fun deleteEmployeeWageHoliday(
            @Path("employeeId") employeeId: Long,
            @Path("wageId") wageId: Long,
            @Path("id") id: Long
    ): Completable

}