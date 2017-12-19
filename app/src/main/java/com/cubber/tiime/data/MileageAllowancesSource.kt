package com.cubber.tiime.data

import android.arch.paging.PositionalDataSource
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
                .subscribe { list, error ->
                    if (list != null) callback.onResult(list, params.requestedStartPosition)
                }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<MileageAllowance>) {
        repository.getMileageAllowances(params.startPosition, count = params.loadSize)
                .subscribe { list, error ->
                    if (list != null) callback.onResult(list)
                }
    }

}
