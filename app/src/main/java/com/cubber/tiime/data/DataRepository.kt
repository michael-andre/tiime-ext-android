package com.cubber.tiime.data

import android.accounts.Account
import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.content.ContentResolver
import android.content.Context
import android.util.Log
import com.cubber.tiime.api.ApiServiceBuilder
import com.cubber.tiime.api.retrofit.UriContentBody
import com.cubber.tiime.auth.AuthManager
import com.cubber.tiime.model.*
import com.cubber.tiime.utils.Month
import com.wapplix.arch.toLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

/**
 * Created by mike on 26/09/17.
 */

class DataRepository(
        accessToken: String,
        private val contentResolver: ContentResolver
) {

    val vehiclesUpdate = PublishSubject.create<Iterable<Vehicle>>()
    val allowancesUpdate = PublishSubject.create<Iterable<MileageAllowance>>()
    val wagesUpdate = PublishSubject.create<Iterable<Wage>>()

    init {
        vehiclesUpdate.subscribe { n -> Log.i("DR", "Subject:" + n.toString()) }
    }

    private val apiService = ApiServiceBuilder.createDataService(accessToken) /*Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()
            .create(TiimeDataService::class.java)*/

    private inline fun <T> Observable<out Any>.mapUpdates(crossinline service: () -> Single<T>): Observable<T> =
            this.flatMapSingle { service() }
                    .startWith(service().toObservable())
                    .replay(1)
                    .autoConnect()

    fun activeVehicles() =
            vehiclesUpdate.mapUpdates {
                apiService.getAssociate()
                        .map { it.vehicles.filter { it.deletionDate == null  } }
            }

    fun associate(): LiveData<Associate> = apiService.getAssociate().toLiveData()

    fun vehicle(id: Long) =
            vehiclesUpdate.mapUpdates {
                apiService.getAssociate()
                        .map { it.vehicles.first { it.id == id } }
            }

    fun saveVehicle(vehicle: Vehicle): Single<Vehicle> {
        return if (vehicle.id > 0L) {
            apiService.updateAssociateVehicle(
                    vehicle.id,
                    name = vehicle.name,
                    card = vehicle.card?.let { card -> UriContentBody(contentResolver, card) }
            ).doOnSuccess { v -> vehiclesUpdate.onNext(listOf(v)) }
        } else {
            apiService.addAssociateVehicle(
                    name = vehicle.name!!,
                    type = vehicle.type!!,
                    fiscalPower = vehicle.fiscalPower!!,
                    card = vehicle.card?.let { card -> UriContentBody(contentResolver, card) }
            ).doOnSuccess { v -> vehiclesUpdate.onNext(listOf(v)) }
        }
    }

    fun deleteVehicle(id: Long): Completable =
            apiService.deleteAssociateVehicle(id)
                    .doOnComplete { vehiclesUpdate.onNext(emptyList()) }

    fun clients(): LiveData<List<Client>> =
            apiService.getClients()
                    .map { it.clients }
                    .toLiveData()

    fun employees() =
            apiService.getEmployees()
                    .map { it.employees }

    fun getEmployeeWages(employeeId: Long, from: Month?, to: Month?): Single<List<Wage>> =
            apiService.getEmployeeWages(employeeId, from, to)
                    .map { it.wages ?: emptyList() }

    fun getEmployeeWagesSource(employeeId: Long) = DataSource.Factory<Month, Wage> {
        WagesSource(this, employeeId)
    }

    fun wage(employeeId: Long, id: Long) =
            apiService.getEmployeeWage(employeeId, id)
                    .toLiveData()

    fun deleteEmployeeWageHoliday(employeeId: Long, wageId: Long, holidayId: Long): Completable =
            apiService.deleteEmployeeWageHoliday(employeeId, wageId, holidayId)
                    .doOnComplete { wagesUpdate.onNext(emptyList()) }

    fun addEmployeeWagesHoliday(employeeId: Long, wageId: Long, holiday: Holiday) =
            apiService.addEmployeeWageHoliday(
                    employeeId,
                    wageId,
                    startDate = holiday.startDate!!,
                    type = holiday.type!!,
                    duration = holiday.duration
            ).doOnSuccess { wagesUpdate.onNext(emptyList()) }

    fun getMileageAllowances(start: Int = 0, count: Int?): Single<List<MileageAllowance>> =
            apiService.getAssociateMileages(start, count)
                    .map { it.mileages ?: emptyList() }

    fun getMileageAllowances() = DataSource.Factory<Int, MileageAllowance> {
        MileageAllowancesSource(this)
    }

    fun addMileageAllowances(allowanceRequest: MileageAllowanceRequest): Single<List<MileageAllowance>> =
            apiService.addAssociateMileages(
                    vehicleId = allowanceRequest.vehicleId,
                    purpose = allowanceRequest.purpose!!,
                    dates = allowanceRequest.dates!!,
                    distance = allowanceRequest.distance!!,
                    fromAddress = allowanceRequest.fromAddress,
                    toAddress = allowanceRequest.toAddress,
                    comment = allowanceRequest.comment,
                    roundTrip = allowanceRequest.roundTrip,
                    polyline = allowanceRequest.polyline
            ).doAfterSuccess { a -> allowancesUpdate.onNext(a) }

    companion object {

        private val repositories = mutableMapOf<Account, DataRepository>()

        fun of(context: Context, account: String? = null): DataRepository {
            val active = AuthManager.getActiveAccount(context) ?: error("No default account")
            return repositories.getOrPut(active) { DataRepository(AuthManager.getAccessToken(context, active), context.contentResolver) }
        }
    }

}
