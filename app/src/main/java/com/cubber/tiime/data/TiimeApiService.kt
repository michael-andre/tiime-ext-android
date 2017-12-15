package com.cubber.tiime.data

import com.cubber.tiime.model.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import java.util.*

/**
 * Created by mike on 05/12/17.
 */
interface TiimeApiService {

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
    fun addAssociateMileage(
            @Body mileageAllowance: MileageAllowance
    ): Single<MileageAllowance>

    @DELETE("associates/me/mileages/{id}")
    fun deleteAssociateMileage(
            @Query("id") id: Long
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
            @Query("id") id: Long,
            @Body vehicle: Vehicle
    ): Single<Vehicle>

    @DELETE("associates/me/vehicles/{id}")
    fun deleteAssociateVehicle(
            @Query("id") id: Long
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
            @Query("id") id: Long,
            @Query("from") from: Date? = null,
            @Query("to") to: Date? = null
    ): Single<WagesList>

    @GET("employees/{employeeId}/wages/{id}")
    fun getEmployeeWage(
            @Query("employeeId") employeeId: Long,
            @Query("id") id: Long
    ): Single<Wage>

    @POST("employees/{employeeId}/wages/{wageId}")
    fun addEmployeeWageHoliday(
            @Query("employeeId") employeeId: Long,
            @Query("wageId") wageId: Long,
            @Body holiday: Holiday
    ): Single<Holiday>

    @DELETE("employees/{employeeId}/wages/{wageId}/holidays/{id}")
    fun deleteEmployeeWageHoliday(
            @Query("employeeId") employeeId: Long,
            @Query("wageId") wageId: Long,
            @Query("id") id: Long
    ): Completable

}