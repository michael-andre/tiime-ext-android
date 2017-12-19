package com.cubber.tiime.model

import android.net.Uri
import android.support.annotation.StringDef
import com.cubber.tiime.utils.Month
import java.math.BigDecimal

/**
 * Created by mike on 26/09/17.
 */

data class Wage(
        var id: Long = 0,
        var period: Month? = null,
        var increase: BigDecimal? = null,
        @Status var status: String? = null,
        @SalaryType var increaseType: String? = null,
        var bonus: BigDecimal? = null,
        @SalaryType var bonusType: String? = null,
        var comment: String? = null,
        var attachment: Uri? = null,
        var holidays: List<Holiday>? = null
) {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SALARY_TYPE_NET, SALARY_TYPE_GROSS)
    annotation class SalaryType

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(STATUS_EDITING, STATUS_VALIDATION_REQUIRED, STATUS_VALIDATED, STATUS_LOCKED)
    annotation class Status

    companion object {

        const val SALARY_TYPE_NET = "net"
        const val SALARY_TYPE_GROSS = "gross"

        const val STATUS_EDITING = "editing"
        const val STATUS_VALIDATION_REQUIRED = "validationRequired"
        const val STATUS_VALIDATED = "validated"
        const val STATUS_LOCKED = "locked"

    }

}
