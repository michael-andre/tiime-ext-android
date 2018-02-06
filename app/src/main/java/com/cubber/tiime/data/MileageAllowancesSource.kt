package com.cubber.tiime.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PositionalDataSource
import android.util.Log
import com.cubber.tiime.model.MileageAllowance
import com.wapplix.arch.Error
import com.wapplix.arch.Loading
import com.wapplix.arch.State
import com.wapplix.arch.Success
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by mike on 21/09/17.
 */
class MileageAllowancesSource(
        private var repository: DataRepository,
        private val state: MutableLiveData<State>
) : PositionalDataSource<MileageAllowance>() {

    private val disp = CompositeDisposable()

    init {
        addInvalidatedCallback { disp.dispose() }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<MileageAllowance>) {
        state.postValue(Loading)
        try {
            val data = repository.getMileageAllowances(0, params.requestedLoadSize).blockingGet()
            state.postValue(Success)
            callback.onResult(data, 0)
            disp.add(repository.allowancesUpdate.firstElement().subscribe { invalidate() })
        } catch (e: Exception) {
            Log.e(MileageAllowancesSource::class.java.simpleName, "Failed to load mileage allowances", e)
            state.postValue(Error(e) { loadInitial(params, callback) })
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<MileageAllowance>) {
        state.postValue(Loading)
        disp.add(repository.getMileageAllowances(params.startPosition, params.loadSize)
                .subscribe { list, e ->
                    if (list != null) {
                        state.postValue(Success)
                        callback.onResult(list)
                    } else if (e != null) {
                        Log.e(MileageAllowancesSource::class.java.simpleName, "Failed to load mileage allowances", e)
                        state.postValue(Error(e) { loadRange(params, callback) })
                    }
                }
        )
    }

}
