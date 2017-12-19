package com.cubber.tiime.api

import com.cubber.tiime.model.*
import com.cubber.tiime.utils.Month
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

/**
 * Created by mike on 05/12/17.
 */
interface TiimeDataService {

    // Associates

    @GET("associates/me")
    fun getAssociate(): Single<Associate>

    // Mileages

    @GET("associates/me/mileages?active=true")
    fun getAssociateMileages(
            @Query("offset") offset: Int? = null,
            @Query("limit") limit: Int? = null
    ): Single<MileageAllowancesList>

    @POST("associates/me/mileages")
    fun addAssociateMileages(
            @Body mileageAllowance: MileageAllowanceRequest
    ): Single<List<MileageAllowance>>

    @DELETE("associates/me/mileages/{id}")
    fun deleteAssociateMileage(
            @Path("id") id: Long
    ): Completable

    // Vehicles

    @POST("associates/me/vehicles?active=true")
    @Multipart
    fun addAssociateVehicle(
            @Body vehicle: Vehicle
    ): Single<Vehicle>

    @PATCH("associates/me/vehicles/{id}")
    @Multipart
    fun updateAssociateVehicle(
            @Path("id") id: Long,
            @Body vehicle: Vehicle
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

    @POST("employees/{employeeId}/wages/{wageId}")
    fun addEmployeeWageHoliday(
            @Path("employeeId") employeeId: Long,
            @Path("wageId") wageId: Long,
            @Body holiday: Holiday
    ): Single<Holiday>

    @DELETE("employees/{employeeId}/wages/{wageId}/holidays/{id}")
    fun deleteEmployeeWageHoliday(
            @Path("employeeId") employeeId: Long,
            @Path("wageId") wageId: Long,
            @Path("id") id: Long
    ): Completable

}