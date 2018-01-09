package com.cubber.tiime.data

import android.arch.paging.ItemKeyedDataSource
import android.util.Log
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.Month
import com.cubber.tiime.utils.month
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class WagesSource(
        private val repository: DataRepository,
        private val employeeId: Long
) : ItemKeyedDataSource<Month, Wage>() {

    override fun getKey(wage: Wage): Month {
        return wage.period ?: error("Invalid item: " + wage)
    }

    override fun loadInitial(params: LoadInitialParams<Month>, callback: LoadInitialCallback<Wage>) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val to = cal.month
        cal.add(Calendar.MONTH, -params.requestedLoadSize)
        val from = cal.month
        load(from, to, callback)
    }

    override fun loadAfter(params: LoadParams<Month>, callback: LoadCallback<Wage>) {
        val cal = Calendar.getInstance()
        cal.time = params.key
        cal.add(Calendar.MONTH, -1)
        val to = cal.month
        cal.add(Calendar.MONTH, -params.requestedLoadSize)
        val from = cal.month
        load(from, to, callback)
    }

    override fun loadBefore(params: LoadParams<Month>, callback: LoadCallback<Wage>) {
        val cal = Calendar.getInstance()
        cal.time = params.key
        cal.add(Calendar.MONTH, 1)
        val from = cal.month
        cal.add(Calendar.MONTH, params.requestedLoadSize)
        val to = cal.month
        load(from, to, callback)
    }

    private fun load(from: Month, to: Month, callback: LoadCallback<Wage>) {
        repository.getEmployeeWages(employeeId, from, to).subscribe { list, e ->
                    if (list != null) callback.onResult(list)
                    else if (e != null) Log.e("WagesSource", "Failed to load wages", e)
                }
    }

}
