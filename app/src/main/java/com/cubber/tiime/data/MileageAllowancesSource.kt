package com.cubber.tiime.data

import android.arch.paging.DataSource
import android.arch.paging.TiledDataSource
import android.content.Context
import com.cubber.tiime.model.MileageAllowance

/**
 * Created by mike on 21/09/17.
 */
class MileageAllowancesSource(context: Context) : TiledDataSource<MileageAllowance>() {

    private val context = context.applicationContext

    override fun countItems(): Int {
        return DataSource.COUNT_UNDEFINED
    }

    override fun loadRange(startPosition: Int, count: Int): List<MileageAllowance>? {
        val allowances = DataRepository.of(context).getMileageAllowances(startPosition, count).blockingGet()
        DataRepository.of(context).allowancesUpdate.firstElement().subscribe {
            invalidate()
        }
        return allowances
    }

}
