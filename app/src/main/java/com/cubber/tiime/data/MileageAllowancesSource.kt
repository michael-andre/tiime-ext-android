package com.cubber.tiime.data

import android.arch.paging.PositionalDataSource
import android.util.Log
import com.cubber.tiime.model.MileageAllowance

/**
 * Created by mike on 21/09/17.
 */
class MileageAllowancesSource(
        private var repository: DataRepository
) : PositionalDataSource<MileageAllowance>() {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<MileageAllowance>) {
        repository.getMileageAllowances(params.requestedStartPosition, count = params.requestedLoadSize)
                .doAfterSuccess {
                    repository.allowancesUpdate.firstElement().subscribe {
                        invalidate()
                    }
                }
                .subscribe { list, e ->
                    if (list != null) callback.onResult(list, params.requestedStartPosition)
                    else if (e != null) Log.e("MileageAllowancesSource", "Failed to load mileages", e)
                }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<MileageAllowance>) {
        repository.getMileageAllowances(params.startPosition, count = params.loadSize)
                .subscribe { list, e ->
                    if (list != null) callback.onResult(list)
                    else if (e != null) Log.e("MileageAllowancesSource", "Failed to load mileages", e)
                }
    }

}
