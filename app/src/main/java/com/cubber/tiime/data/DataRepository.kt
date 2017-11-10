package com.cubber.tiime.data

import android.arch.lifecycle.LiveData
import android.content.Context
import com.cubber.tiime.model.*
import com.google.common.base.Optional
import com.google.maps.model.EncodedPolyline
import com.wapplix.arch.SimpleData
import com.wapplix.arch.map
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mike on 26/09/17.
 */

class DataRepository {

    private val vehicles: List<Vehicle> = listOf(
            Vehicle(id = 1, name = "Batmobile", type = Vehicle.TYPE_CAR, fiscal_power = Vehicle.FISCAL_POWER_4),
            Vehicle(id = 2, name = "Batmoto", type = Vehicle.TYPE_TWO_WHEELER_2, fiscal_power = Vehicle.FISCAL_POWER_3_4_5)
    )

    private val clients: List<Client> = listOf(
            Client(id = 1, name = "Google", address = "Rue de Londres, Paris"),
            Client(id = 2, name = "Apple", address = "Rue de Rivoli, Paris")
    )

    private val employees: List<Employee> = listOf(
            Employee(id = 1, name = "Peter Parker", wagesValidationRequired = true),
            Employee(id = 2, name = "Bruce Wayne")
    )

    private val wages: Map<Long, List<Wage>> = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        mapOf(
                1L to listOf(
                        Wage(id = 110, period = dateFormat.parse("2017-10-01"), holidays = listOf(
                                Holiday(id = 1101, startDate = dateFormat.parse("2017-10-25"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1102, startDate = dateFormat.parse("2017-10-14"), type = Holiday.TYPE_SICK_LEAVE, duration = 4),
                                Holiday(id = 1103, startDate = dateFormat.parse("2017-10-20"), type = Holiday.TYPE_UNPAID_HOLIDAY, duration = 6)
                        ), editable = true, increase = BigDecimal("300"), increaseType = Wage.SALARY_TYPE_GROSS, grossSalary = BigDecimal("3000")),
                        Wage(id = 109, period = dateFormat.parse("2017-09-01"), holidays = listOf(
                                Holiday(id = 1091, startDate = dateFormat.parse("2017-09-12"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1092, startDate = dateFormat.parse("2017-09-09"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 6),
                                Holiday(id = 1093, startDate = dateFormat.parse("2017-09-20"), type = Holiday.TYPE_PAID_VACATION, duration = 10)
                        ), validated = true, editable = true, comment = "Commentaire sur ce mois"),
                        Wage(id = 108, period = dateFormat.parse("2017-08-01"), holidays = listOf(
                                Holiday(id = 1081, startDate = dateFormat.parse("2017-08-03"), type = Holiday.TYPE_PAID_VACATION, duration = 4),
                                Holiday(id = 1082, startDate = dateFormat.parse("2017-08-06"), type = Holiday.TYPE_SICK_LEAVE, duration = 2),
                                Holiday(id = 1083, startDate = dateFormat.parse("2017-08-10"), type = Holiday.TYPE_WORK_ACCIDENT, duration = 1),
                                Holiday(id = 1084, startDate = dateFormat.parse("2017-08-11"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 3),
                                Holiday(id = 1085, startDate = dateFormat.parse("2017-08-20"), type = Holiday.TYPE_PAID_VACATION, duration = 12)
                        ), validated = true, bonus = BigDecimal("5000"), bonusType = Wage.SALARY_TYPE_NET)
                )
        )
    }()

    private val mileageAllowances: List<MileageAllowance> = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        listOf(
                MileageAllowance(
                        id = 1,
                        reason = "Apple",
                        polyline = EncodedPolyline("kufcHe|nL}@qSeBL{C|A}GnCcFv@mIx@gDsGyCkQcGmVkGc|@qCo`@cA{M~BUnI|@dGvE~OpPnJkEdAgAEoBJkD~AiAzAnAvA`Pi@dT{EhVgMbYsl@ts@eZ~d@_Zrq@oRfo@eKdl@mEfd@eNbeCd@fV`DxWvQj_A`F~u@Cxi@qGjqCYxVpAh^tFzo@Xt[{@bWoLlvAg@nj@nAjaAJr]wA`TeFpWqLjYqJlYwI|c@{R`~@}G`QiLdh@_Trj@uDpNuAnN@bQbEti@s@zg@hDhsAv@hvAeAdHyBrD_EbBeEIqJOmHzBsDxD{DpJiFrRaMpYeE`PqDd^eDh[uC~KuPja@iNpT{EpMkAlId@r^UjZl@|KnC|GbEnCbIEnBfAa@rHcd@jqAaCjR}EhcAuDrq@iEnF}QyBe`@mNkk@uVmg@{KmS_BuZCyRpAc]lG_ZxJof@vUey@f_@_u@tTyy@dMkr@tCov@}Ag\\sCsu@mNcp@_PynAcXsR{Bs\\_Cac@q@ufAlCelAfBao@_Dig@_Haz@sKav@_C{k@xA_}@jHsx@q@co@U_o@jDeu@hLab@lIaS`Ewe@fG}ZhAmXWsv@kIqfAaOu`A}@qe@a@}]wCc\\cGu`@qLcp@_Rkm@yH}nCeKw~@kEsw@mJml@wLccAw[wu@s]_r@m^uw@_]an@cOczB}[epAwVswBgm@s}@cXah@}Jqw@uFgv@gB{pAU_`@lCeY|FeZ~I_a@tHaZfBkWKaa@uCca@e@aV~Ak\\rG}VdIyMnBqO]wLwCqMqHeKwKcMsUqRqo@yLmi@uOol@gEeLaImYeTmbAgHil@qEap@{JyaAkM_p@w`@ivAcYgu@mVmi@}Z{w@_Z}mA_\\qfAk]geAoT_lA{VsgBy[q}BkSokAuGwUqOu_@oTk]ip@ip@ul@kn@gQuWw`@cv@qr@avAuXke@_PmSun@oi@sYyYmiAw`BoLiR{]su@k\\wiA}Rat@wHeMoIoHir@yT__@eM{P_Jo@wE|@kHzOwp@nKcp@hHwz@jH{Mn@aFuAkHsEgBaKuAsRyI{P{M_SwUeKkPme@gy@iQe]uNcUuTyMsz@ks@cq@qm@sl@um@qReHwm@eFif@oCkNn@_QlE{Q~Fy]cCiPnFaW|AkPaEg\\{ZcMyNwJeSkNwLBya@k`@acBaVkg@_Jc^SwGeBaGmBXeAhDdAnJuAvGeJzIuj@tv@q\\h`@_O~SeSdW_GrCiH|H{BlC_@e@k@z@cBzCk@rAg@pA{Lxd@oB`Hq@PwHqDgAhHgFtWcFdUgDbO"),
                        distance = 30,
                        dates = setOf(dateFormat.parse("2017-08-14"))
                ),
                MileageAllowance(
                        id = 0,
                        reason = "Visite client",
                        polyline = EncodedPolyline("itg`H{vdCvAsGYqDKeA{AmLkO|DqA_Hc@cl@~@_f@jLqCjAe@zHkJmTsHyt@ck@{Lo[gIe]{OcV_p@uTwnCej@i[}P_b@yv@{Waa@mb@ma@{l@wp@{^}o@eUui@_`@{nAcZ{yAs\\_pBa^inBcb@{jBm[afCmIwi@{Y{iAke@_~Ai_@mlByQcu@qy@u}Bej@e~A_[kqAwc@eoBos@iqC_g@}mAac@ey@mx@wkBmiAuqC_a@uhAkb@m_B{`@o_C}P}iB{EeaCfBcz@q@kkByOmzBpBcmCdK{dDrDedCeDwbBsI}lAoJmk@kPeh@mYeg@{a@_`@{]aV{JaIkQmP_j@ir@yn@yw@iv@iq@qa@_d@sd@wt@u|Aq`EevA}aCkZg}@{Rqy@al@ubBwi@cdAq`@m`Aoe@wsA{b@o|@_iB}pCq_AmyBmc@w_A_\\kh@izAwcB{`@yb@gaAkpA}pAgyCqf@_~@k{@ekAcv@wqAof@}qAea@_cAy]{p@wcAuaBue@e`A{^qhAwWaqAyL_fAqU_`EdEqeCkFm`@sNcYkN_Km]eB}v@|C{Y{@wq@qAwiArEqr@{F_kAoc@sj@ePwg@}Dkn@|Cg{@fX_fBdw@wv@fRaz@hJs|A}@g\\kDqgAkUokBoa@kt@sFigAl@_rApDaxAaHgs@yKw_AqGqs@h@cp@pF}y@bBeaBAc{AtTaq@rMm{@`Fmt@_EqlAcRut@wB{hAiA_~@gOwyAi`@whCeMkcCoNq{@yO_q@mReoBo}@q{@ua@ak@oPmlEms@iyEyqA}o@oIceAkD_gAu@_cArFuu@fSey@`Ho|@qEku@dDup@nQq]O_\\kQ}Ysj@es@glCmV}bA{NagAsGy`AsOwgAm]msAioAq_Dgt@ipCg_@{eAaXgnAys@_fFiTetAkQwm@mXih@oYu\\s}@q{@iZoa@ucAkpByl@ahAkY{]k^oZaFuKaOg@od@rEqe@xTsSYgYuLgUoC{]l@a\\cCwJjCiXbXaLlQkEnW{Q~r@uSzQ}r@hZyUsCkNoN{m@g}Awm@{|A{I_]|Cuh@vBgf@iDmUwJcMac@oFmLoDuH{HgQwPkQoD_S~AaM~MaPlZmQxAgb@eDuVjDq_Aha@ibAv\\gSuEmPqGuWkOwNcJiB}FaFwLoL|FsDwAsCkKqGeGgF_U|Ry^|Msg@iI{s@aYgg@yTe`@gg@oh@um@gl@kZk^ol@wy@yWk]gJ{d@{D_tBd@wOgGeFyMaG_EiHuCaZkBwQyFwBcFhFq@z@eES}Jb@cDvHwCvM"),
                        distance = 250,
                        dates = setOf(dateFormat.parse("2017-08-12"))
                )
        )
    }()

    fun vehicles(): LiveData<List<Vehicle>> {
        return SimpleData(vehicles)
    }

    fun defaultVehicleId(): LiveData<Optional<Long>> {
        return SimpleData(Optional.of(vehicles[0].id))
    }

    fun officeAddress(): LiveData<Optional<String>> {
        return SimpleData(Optional.of("75 boulevard Haussmann, Paris"))
    }

    fun vehicle(id: Long): LiveData<Optional<Vehicle>> {
        return vehicles().map { vehicles -> Optional.fromNullable(vehicles.firstOrNull { it.id == id }) }
    }

    fun clients(): LiveData<List<Client>> {
        return SimpleData(clients)
    }

    fun vehicleTypes(): LiveData<List<String>> {
        return SimpleData(Arrays.asList(Vehicle.TYPE_CAR, Vehicle.TYPE_TWO_WHEELER_1, Vehicle.TYPE_TWO_WHEELER_2))
    }

    fun employees(): LiveData<List<Employee>> {
        return SimpleData(employees)
    }

    fun getEmployeeWages(employeeId: Long, from: Date?, to: Date?): List<Wage> {
        return wages[employeeId]?.filter { w ->
            (from == null || !w.period!!.before(from))
                    && (to == null || !w.period!!.after(to))
        } ?: emptyList()
    }

    fun wage(id: Long): LiveData<Wage?> {
        return SimpleData(wages.values.flatten().firstOrNull { it.id == id })
    }

    fun getMileageAllowances(start: Int = 0, count: Int?): List<MileageAllowance> {
        val fromIndex = minOf(start, mileageAllowances.size - 1)
        val toIndex = minOf(start + (count ?: Int.MAX_VALUE), mileageAllowances.size)
        return mileageAllowances.subList(fromIndex, toIndex)
    }

    companion object {

        fun of(context: Context): DataRepository {
            return DataRepository()
        }
    }

}
