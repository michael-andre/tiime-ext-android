package com.cubber.tiime.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.ItemKeyedDataSource
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.Month
import com.cubber.tiime.utils.month
import com.wapplix.arch.Error
import com.wapplix.arch.Loading
import com.wapplix.arch.State
import com.wapplix.arch.Success
import io.reactivex.disposables.CompositeDisposable
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class WagesSource(
        private val repository: DataRepository,
        private val employeeId: Long,
        private val state: MutableLiveData<State>
) : ItemKeyedDataSource<Month, Wage>() {

    private val disp = CompositeDisposable()

    init {
        addInvalidatedCallback { disp.dispose() }
    }

    override fun getKey(wage: Wage): Month {
        return wage.period ?: error("Invalid item: " + wage)
    }

    override fun loadInitial(params: LoadInitialParams<Month>, callback: LoadInitialCallback<Wage>) {
        state.postValue(Loading)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        val to = cal.month
        cal.add(Calendar.MONTH, -params.requestedLoadSize)
        val from = cal.month
        try {
            val wages = repository.getEmployeeWages(employeeId, from, to).blockingGet()
            state.postValue(Success)
            callback.onResult(wages)
            disp.add(repository.wagesUpdate.subscribe { invalidate() })
        } catch (e: Exception) {
            state.postValue(Error(e) { loadInitial(params, callback) })
        }
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
        state.postValue(Loading)
        disp.add(repository.getEmployeeWages(employeeId, from, to).subscribe { list, e ->
            if (list != null) {
                state.postValue(Success)
                callback.onResult(list)
            } else if (e != null) {
                state.postValue(Error(e) { load(from, to, callback) })
            }
        })
    }

}
