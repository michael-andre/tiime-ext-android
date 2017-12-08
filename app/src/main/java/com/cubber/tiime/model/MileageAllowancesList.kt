package com.cubber.tiime.model

import java.math.BigDecimal

/**
 * Created by mike on 21/09/17.
 */

data class MileageAllowancesList(
        var mileages: List<MileageAllowance>? = null,
        var annualCounts: Map<Int, AnnualCount>? = null
) {

    data class AnnualCount(
        var distance: Int? = null,
        var amount: BigDecimal? = null
    )

}