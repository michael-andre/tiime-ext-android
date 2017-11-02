package com.cubber.tiime.data

import android.arch.paging.TiledDataSource
import android.content.Context
import com.cubber.tiime.model.MileageAllowance

/**
 * Created by mike on 21/09/17.
 */
class MileageAllowancesSource(context: Context) : TiledDataSource<MileageAllowance>() {

    private val context = context.applicationContext

    override fun countItems(): Int {
        return 2
    }

    override fun loadRange(startPosition: Int, count: Int): List<MileageAllowance>? {
        return DataRepository.of(context).getMileageAllowances(startPosition, count)
    }

}
