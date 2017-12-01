package com.cubber.tiime.model

import android.support.annotation.StringDef
import java.math.BigDecimal
import java.util.*

/**
 * Created by mike on 26/09/17.
 */

data class Wage(
    var id: Long = 0,
    var period: Date? = null,
    var increase: BigDecimal? = null,
    @SalaryType var increaseType: String? = null,
    var bonus: BigDecimal? = null,
    @SalaryType var bonusType: String? = null,
    var comment: String? = null,
    var validated: Boolean = false,
    var editable: Boolean = false,
    var holidays: List<Holiday>? = null
) {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SALARY_TYPE_NET, SALARY_TYPE_GROSS)
    annotation class SalaryType

    companion object {
        const val SALARY_TYPE_NET = "net"
        const val SALARY_TYPE_GROSS = "gross"
    }

}
