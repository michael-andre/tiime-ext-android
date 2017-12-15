package com.cubber.tiime.data

import com.cubber.tiime.model.*
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mike on 05/12/17.
 */
object FakeApiService : TiimeApiService {

    override fun getAssociate(): Single<Associate> =
            Single.fromCallable {
                me.copy()
            }

    override fun getAssociateMileages(offset: Int?, limit: Int?): Single<MileageAllowancesList> =
            Single.fromCallable {
                val fromIndex = offset ?: 0
                if (fromIndex >= mileageAllowances.size) return@fromCallable MileageAllowancesList(emptyList())
                val toIndex = minOf((offset ?: 0) + (limit ?: Int.MAX_VALUE), mileageAllowances.size)
                MileageAllowancesList(mileageAllowances.subList(fromIndex, toIndex).toList())
            }

    override fun addAssociateMileage(mileageAllowance: MileageAllowance): Single<MileageAllowance> =
            Single.fromCallable {
                val copy = mileageAllowance.copy(
                        id = (mileageAllowances.map { it.id }.max() ?: 0) + 1
                )
                mileageAllowances = mileageAllowances.asSequence()
                        .plus(copy)
                        .sortedByDescending { it.dates?.max() }
                        .toList()
                copy
            }

    override fun deleteAssociateMileage(id: Long): Completable =
            Completable.fromAction {
                mileageAllowances = mileageAllowances.minus(mileageAllowances.filter { it.id == id })
            }

    override fun addAssociateVehicle(vehicle: Vehicle): Single<Vehicle> =
            Single.fromCallable {
                val copy = vehicle.copy(
                        id = (me.vehicles.map { it.id }.max() ?: 0) + 1
                )
                me = me.copy(
                        vehicles = me.vehicles.asSequence()
                                .plus(copy)
                                .sortedBy { it.name }
                                .toList()
                )
                copy
            }

    override fun updateAssociateVehicle(id: Long, vehicle: Vehicle): Single<Vehicle> =
            Single.fromCallable {
                val copy = vehicle.copy(id = id)
                me = me.copy(
                        vehicles = me.vehicles.asSequence()
                                .map { if (it.id == id) copy else it }
                                .sortedBy { it.name }
                                .toList()
                )
                copy
            }

    override fun deleteAssociateVehicle(id: Long): Completable =
            Completable.fromAction {
                me = me.copy(
                        vehicles = me.vehicles.filter { it.id != id }
                )
            }

    override fun getClients(): Single<ClientsList> =
            Single.fromCallable {
                ClientsList(clients.toList())
            }

    override fun getEmployees(): Single<EmployeesList> =
            Single.fromCallable {
                EmployeesList(employees.toList())
            }

    override fun getEmployeeWages(id: Long, from: Date?, to: Date?): Single<WagesList> =
            Single.fromCallable {
                WagesList(wages[id]?.filter { w ->
                    (from == null || !w.period!!.before(from))
                            && (to == null || !w.period!!.after(to))
                } ?: emptyList())
            }

    override fun deleteEmployeeWageHoliday(employeeId: Long, wageId: Long, id: Long): Completable =
            Completable.fromAction {
                wages[employeeId] = wages[employeeId]?.map {
                    if (it.id == wageId)
                        it.copy(
                            holidays = it.holidays?.filter { it.id != id }
                        )
                    else it
                }.orEmpty()
            }

    override fun addEmployeeWageHoliday(employeeId: Long, wageId: Long, holiday: Holiday): Single<Holiday> =
            Single.fromCallable {
                val copy = holiday.copy(
                        id = (wages.values.asSequence().flatten().map { it.holidays.orEmpty() }.flatten().map { it.id }.max() ?: 0) + 1
                )
                wages[employeeId] = wages[employeeId]?.map {
                    if (it.id == wageId)
                        it.copy(
                                holidays = it.holidays.orEmpty().plus(copy)
                        )
                    else it
                }.orEmpty()
                copy
            }

    override fun getEmployeeWage(employeeId: Long, id: Long): Single<Wage> =
            Single.fromCallable {
                wages[employeeId]!!.first { it.id == id }.copy()
            }

    private var me = Associate(
            id = 1,
            name = "James Bond",
            defaultFromAddress = "Avenue des Champs Elysées, Paris",
            defaultVehicleId = 1,
            vehicles = listOf(
                    Vehicle(id = 1, name = "Batmobile", type = Vehicle.TYPE_CAR, fiscalPower = Vehicle.FISCAL_POWER_4),
                    Vehicle(id = 2, name = "Batmoto", type = Vehicle.TYPE_TWO_WHEELER_2, fiscalPower = Vehicle.FISCAL_POWER_3_4_5)
            )
    )

    private val clients = listOf(
            Client(id = 1, name = "Google", directionsAddress = "Rue de Londres, Paris"),
            Client(id = 2, name = "Apple", directionsAddress = "Rue de Rivoli, Paris")
    )

    private val employees = listOf(
            Employee(id = 1, name = "Peter Parker", wagesValidationRequired = true),
            Employee(id = 2, name = "Bruce Wayne")
    )

    private val wages = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        mutableMapOf(
                1L to listOf(
                        Wage(id = 110, period = dateFormat.parse("2017-10-01"), holidays = listOf(
                                Holiday(id = 1101, startDate = dateFormat.parse("2017-10-25"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1102, startDate = dateFormat.parse("2017-10-14"), type = Holiday.TYPE_SICK_LEAVE, duration = 4),
                                Holiday(id = 1103, startDate = dateFormat.parse("2017-10-20"), type = Holiday.TYPE_UNPAID_HOLIDAY, duration = 6)
                        ), status = Wage.STATUS_VALIDATION_REQUIRED, increase = BigDecimal("300"), increaseType = Wage.SALARY_TYPE_GROSS),
                        Wage(id = 109, period = dateFormat.parse("2017-09-01"), holidays = listOf(
                                Holiday(id = 1091, startDate = dateFormat.parse("2017-09-12"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1092, startDate = dateFormat.parse("2017-09-09"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 6),
                                Holiday(id = 1093, startDate = dateFormat.parse("2017-09-20"), type = Holiday.TYPE_PAID_VACATION, duration = 10)
                        ), status = Wage.STATUS_VALIDATED, comment = "Commentaire sur ce mois"),
                        Wage(id = 108, period = dateFormat.parse("2017-08-01"), holidays = listOf(
                                Holiday(id = 1081, startDate = dateFormat.parse("2017-08-03"), type = Holiday.TYPE_PAID_VACATION, duration = 4),
                                Holiday(id = 1082, startDate = dateFormat.parse("2017-08-06"), type = Holiday.TYPE_SICK_LEAVE, duration = 2),
                                Holiday(id = 1083, startDate = dateFormat.parse("2017-08-10"), type = Holiday.TYPE_WORK_ACCIDENT, duration = 1),
                                Holiday(id = 1084, startDate = dateFormat.parse("2017-08-11"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 3),
                                Holiday(id = 1085, startDate = dateFormat.parse("2017-08-20"), type = Holiday.TYPE_PAID_VACATION, duration = 12)
                        ), status = Wage.STATUS_LOCKED, bonus = BigDecimal("5000"), bonusType = Wage.SALARY_TYPE_NET),
                        Wage(id = 107, period = dateFormat.parse("2017-07-01"), holidays = listOf(), status = Wage.STATUS_LOCKED, comment = "Oups")
                )
        )
    }()

    private var mileageAllowances = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        listOf(
                MileageAllowance(
                        id = 1,
                        purpose = "Apple",
                        fromAddress = "48, rue de Provence, Paris",
                        toAddress = "82, rue Beaubourg, Paris",
                        distance = 30,
                        dates = setOf(dateFormat.parse("2017-08-14"))
                ),
                MileageAllowance(
                        id = 0,
                        purpose = "Visite client",
                        distance = 250,
                        dates = setOf(dateFormat.parse("2017-08-12"))
                )
        )
    }()
}