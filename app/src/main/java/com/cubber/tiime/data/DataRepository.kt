package com.cubber.tiime.data

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.cubber.tiime.model.*
import com.wapplix.arch.toLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Created by mike on 26/09/17.
 */

class DataRepository {

    val vehiclesUpdate = PublishSubject.create<Iterable<Vehicle>>()
    val allowancesUpdate = PublishSubject.create<Iterable<MileageAllowance>>()
    val wagesUpdate = PublishSubject.create<Iterable<Wage>>()

    init {
        vehiclesUpdate.subscribe { n -> Log.i("DR", "Subject:" + n.toString()) }
    }

    private val apiService = /*Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()
            .create(TiimeApiService::class.java)*/FakeApiService

    private inline fun <T> Observable<out Any>.mapUpdates(crossinline service: () -> Single<T>): Observable<T> =
            this.flatMapSingle { service() }
                    .startWith(service().toObservable())
                    .replay(1)
                    .autoConnect()

    fun vehicles() =
            vehiclesUpdate.mapUpdates {
                apiService.getAssociate().map { it.vehicles }
            }

    fun associate(): LiveData<Associate> = apiService.getAssociate().toLiveData()

    fun vehicle(id: Long) =
            vehiclesUpdate.mapUpdates {
                apiService.getAssociate().map { it.vehicles.first { it.id == id } }
            }

    fun saveVehicle(vehicle: Vehicle): Single<Vehicle> {
        return if (vehicle.id > 0L) {
            apiService.updateAssociateVehicle(vehicle.id, vehicle)
        } else {
            apiService.addAssociateVehicle(vehicle)
        }.doOnSuccess { v -> vehiclesUpdate.onNext(listOf(v)) }
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

    fun getEmployeeWages(employeeId: Long, from: Date?, to: Date?): Observable<List<Wage>> =
            apiService.getEmployeeWages(employeeId, from, to)
                    .map { it.wages ?: emptyList() }
                    .toObservable()

    fun wage(employeeId: Long, id: Long) =
            apiService.getEmployeeWage(employeeId, id)
                    .toLiveData()

    fun deleteEmployeeWageHoliday(employeeId: Long, wageId: Long, holidayId: Long): Completable =
            apiService.deleteEmployeeWageHoliday(employeeId, wageId, holidayId)
                    .doOnComplete { wagesUpdate.onNext(emptyList()) }

    fun addEmployeeWagesHoliday(employeeId: Long, wageId: Long, holiday: Holiday) =
            apiService.addEmployeeWageHoliday(employeeId, wageId, holiday)
                    .doOnSuccess { wagesUpdate.onNext(emptyList()) }

    fun getMileageAllowances(start: Int = 0, count: Int?): Single<List<MileageAllowance>> =
            apiService.getAssociateMileages(start, count)
                    .map { it.mileages ?: emptyList() }

    fun saveAllowance(allowance: MileageAllowance) =
            apiService.addAssociateMileage(allowance)
                    .doAfterSuccess { a -> allowancesUpdate.onNext(listOf(a)) }

    companion object {

        private val repository by lazy { DataRepository() }

        fun of(context: Context): DataRepository {
            return repository
        }
    }

}
