package com.cubber.tiime.data

import android.arch.paging.KeyedDataSource
import android.content.Context
import com.cubber.tiime.model.Wage
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class WagesSource(context: Context, private val employeeId: Long) : KeyedDataSource<Date, Wage>() {

    private val context = context.applicationContext

    override fun getKey(wage: Wage): Date {
        return wage.period ?: throw IllegalStateException("Invalid item: " + wage)
    }

    override fun loadInitial(pageSize: Int): List<Wage>? {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val to = cal.time
        cal.add(Calendar.MONTH, -pageSize)
        val from = cal.time
        return load(from, to)
    }

    override fun loadAfter(currentEndKey: Date, pageSize: Int): List<Wage>? {
        val cal = Calendar.getInstance()
        cal.time = currentEndKey
        cal.add(Calendar.MONTH, -1)
        val to = cal.time
        cal.add(Calendar.MONTH, -pageSize)
        val from = cal.time
        return load(from, to)
    }

    override fun loadBefore(currentBeginKey: Date, pageSize: Int): List<Wage>? {
        val cal = Calendar.getInstance()
        cal.time = currentBeginKey
        cal.add(Calendar.MONTH, 1)
        val from = cal.time
        cal.add(Calendar.MONTH, pageSize)
        val to = cal.time
        return load(from, to).reversed()
    }

    private fun load(from: Date, to: Date): List<Wage> {
        return DataRepository.of(context).getEmployeeWages(employeeId, from, to).blockingFirst()
    }

}
