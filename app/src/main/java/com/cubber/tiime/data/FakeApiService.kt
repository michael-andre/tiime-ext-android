package com.cubber.tiime.data

import com.cubber.tiime.model.*
import com.google.maps.model.EncodedPolyline
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mike on 05/12/17.
 */
object FakeApiService : TiimeApiService {

    override fun getAssociate(): Single<Associate> =
            Single.just(me)

    override fun getAssociateMileages(offset: Int?, limit: Int?): Single<MileageAllowancesList> {
        val fromIndex = minOf(offset ?: 0, mileageAllowances.size - 1)
        val toIndex = minOf((offset ?: 0) + (limit ?: Int.MAX_VALUE), mileageAllowances.size)
        return Single.just(MileageAllowancesList(mileageAllowances.subList(fromIndex, toIndex)))
    }

    override fun addAssociateMileage(mileageAllowance: MileageAllowance): Single<MileageAllowance> {
        return Single.fromCallable {
            mileageAllowance.id = (mileageAllowances.map { it.id }.max() ?: 0) + 1
            mileageAllowances.add(mileageAllowance)
            mileageAllowance
        }
    }

    override fun deleteAssociateMileage(id: Long): Completable {
        return Completable.fromAction {
            mileageAllowances.removeAll { it.id == id }
        }
    }

    override fun addAssociateVehicle(vehicle: Vehicle): Single<Vehicle> {
        return Single.fromCallable {
            vehicle.id = (me.vehicles.map { it.id }.max() ?: 0) + 1
            (me.vehicles as MutableList).add(vehicle)
            vehicle
        }
    }

    override fun updateAssociateVehicle(id: Long, vehicle: Vehicle): Single<Vehicle> {
        return Single.fromCallable {
            val i = me.vehicles.indexOfFirst { it.id == id }
            if (i >= 0) (me.vehicles as MutableList)[i] = vehicle
            vehicle
        }
    }

    override fun deleteAssociateVehicle(id: Long): Completable {
        return Completable.fromAction {
            (me.vehicles as MutableList).removeAll { it.id == id }
        }
    }

    override fun getClients(): Single<ClientsList> =
            Single.just(ClientsList(clients))

    override fun getEmployees(): Single<EmployeesList> =
            Single.just(EmployeesList(employees))

    override fun getEmployeeWages(id: Long, from: Date?, to: Date?): Single<WagesList> {
        return Single.just(WagesList(wages[id]?.filter { w ->
            (from == null || !w.period!!.before(from))
                    && (to == null || !w.period!!.after(to))
        } ?: emptyList()))
    }

    override fun getEmployeeWage(employeeId: Long, id: Long): Single<Wage> =
            Single.just(wages.values.flatten().first { it.id == id })

    private val vehicles = mutableListOf(
            Vehicle(id = 1, name = "Batmobile", type = Vehicle.TYPE_CAR, fiscalPower = Vehicle.FISCAL_POWER_4),
            Vehicle(id = 2, name = "Batmoto", type = Vehicle.TYPE_TWO_WHEELER_2, fiscalPower = Vehicle.FISCAL_POWER_3_4_5)
    )

    private val me = Associate(id = 1, name = "James Bond", defaultFromAddress = "Avenue des Champs Elysées, Paris", defaultVehicleId = 1, vehicles = vehicles)

    private val clients = mutableListOf(
            Client(id = 1, name = "Google", directionsAddress = "Rue de Londres, Paris"),
            Client(id = 2, name = "Apple", directionsAddress = "Rue de Rivoli, Paris")
    )

    private val employees = mutableListOf(
            Employee(id = 1, name = "Peter Parker", wagesValidationRequired = true),
            Employee(id = 2, name = "Bruce Wayne")
    )

    private val wages = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        mapOf(
                1L to mutableListOf(
                        Wage(id = 110, period = dateFormat.parse("2017-10-01"), holidays = mutableListOf(
                                Holiday(id = 1101, startDate = dateFormat.parse("2017-10-25"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1102, startDate = dateFormat.parse("2017-10-14"), type = Holiday.TYPE_SICK_LEAVE, duration = 4),
                                Holiday(id = 1103, startDate = dateFormat.parse("2017-10-20"), type = Holiday.TYPE_UNPAID_HOLIDAY, duration = 6)
                        ), editable = true, increase = BigDecimal("300"), increaseType = Wage.SALARY_TYPE_GROSS),
                        Wage(id = 109, period = dateFormat.parse("2017-09-01"), holidays = mutableListOf(
                                Holiday(id = 1091, startDate = dateFormat.parse("2017-09-12"), type = Holiday.TYPE_FAMILY_MATTERS, duration = 1),
                                Holiday(id = 1092, startDate = dateFormat.parse("2017-09-09"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 6),
                                Holiday(id = 1093, startDate = dateFormat.parse("2017-09-20"), type = Holiday.TYPE_PAID_VACATION, duration = 10)
                        ), validated = true, editable = true, comment = "Commentaire sur ce mois"),
                        Wage(id = 108, period = dateFormat.parse("2017-08-01"), holidays = mutableListOf(
                                Holiday(id = 1081, startDate = dateFormat.parse("2017-08-03"), type = Holiday.TYPE_PAID_VACATION, duration = 4),
                                Holiday(id = 1082, startDate = dateFormat.parse("2017-08-06"), type = Holiday.TYPE_SICK_LEAVE, duration = 2),
                                Holiday(id = 1083, startDate = dateFormat.parse("2017-08-10"), type = Holiday.TYPE_WORK_ACCIDENT, duration = 1),
                                Holiday(id = 1084, startDate = dateFormat.parse("2017-08-11"), type = Holiday.TYPE_COMPENSATORY_TIME, duration = 3),
                                Holiday(id = 1085, startDate = dateFormat.parse("2017-08-20"), type = Holiday.TYPE_PAID_VACATION, duration = 12)
                        ), validated = true, bonus = BigDecimal("5000"), bonusType = Wage.SALARY_TYPE_NET)
                )
        )
    }()

    private val mileageAllowances = {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        mutableListOf(
                MileageAllowance(
                        id = 1,
                        purpose = "Apple",
                        polyline = EncodedPolyline("kufcHe|nL}@qSeBL{C|A}GnCcFv@mIx@gDsGyCkQcGmVkGc|@qCo`@cA{M~BUnI|@dGvE~OpPnJkEdAgAEoBJkD~AiAzAnAvA`Pi@dT{EhVgMbYsl@ts@eZ~d@_Zrq@oRfo@eKdl@mEfd@eNbeCd@fV`DxWvQj_A`F~u@Cxi@qGjqCYxVpAh^tFzo@Xt[{@bWoLlvAg@nj@nAjaAJr]wA`TeFpWqLjYqJlYwI|c@{R`~@}G`QiLdh@_Trj@uDpNuAnN@bQbEti@s@zg@hDhsAv@hvAeAdHyBrD_EbBeEIqJOmHzBsDxD{DpJiFrRaMpYeE`PqDd^eDh[uC~KuPja@iNpT{EpMkAlId@r^UjZl@|KnC|GbEnCbIEnBfAa@rHcd@jqAaCjR}EhcAuDrq@iEnF}QyBe`@mNkk@uVmg@{KmS_BuZCyRpAc]lG_ZxJof@vUey@f_@_u@tTyy@dMkr@tCov@}Ag\\sCsu@mNcp@_PynAcXsR{Bs\\_Cac@q@ufAlCelAfBao@_Dig@_Haz@sKav@_C{k@xA_}@jHsx@q@co@U_o@jDeu@hLab@lIaS`Ewe@fG}ZhAmXWsv@kIqfAaOu`A}@qe@a@}]wCc\\cGu`@qLcp@_Rkm@yH}nCeKw~@kEsw@mJml@wLccAw[wu@s]_r@m^uw@_]an@cOczB}[epAwVswBgm@s}@cXah@}Jqw@uFgv@gB{pAU_`@lCeY|FeZ~I_a@tHaZfBkWKaa@uCca@e@aV~Ak\\rG}VdIyMnBqO]wLwCqMqHeKwKcMsUqRqo@yLmi@uOol@gEeLaImYeTmbAgHil@qEap@{JyaAkM_p@w`@ivAcYgu@mVmi@}Z{w@_Z}mA_\\qfAk]geAoT_lA{VsgBy[q}BkSokAuGwUqOu_@oTk]ip@ip@ul@kn@gQuWw`@cv@qr@avAuXke@_PmSun@oi@sYyYmiAw`BoLiR{]su@k\\wiA}Rat@wHeMoIoHir@yT__@eM{P_Jo@wE|@kHzOwp@nKcp@hHwz@jH{Mn@aFuAkHsEgBaKuAsRyI{P{M_SwUeKkPme@gy@iQe]uNcUuTyMsz@ks@cq@qm@sl@um@qReHwm@eFif@oCkNn@_QlE{Q~Fy]cCiPnFaW|AkPaEg\\{ZcMyNwJeSkNwLBya@k`@acBaVkg@_Jc^SwGeBaGmBXeAhDdAnJuAvGeJzIuj@tv@q\\h`@_O~SeSdW_GrCiH|H{BlC_@e@k@z@cBzCk@rAg@pA{Lxd@oB`Hq@PwHqDgAhHgFtWcFdUgDbO"),
                        distance = 30,
                        dates = setOf(dateFormat.parse("2017-08-14"))
                ),
                MileageAllowance(
                        id = 0,
                        purpose = "Visite client",
                        polyline = EncodedPolyline("itg`H{vdCvAsGYqDKeA{AmLkO|DqA_Hc@cl@~@_f@jLqCjAe@zHkJmTsHyt@ck@{Lo[gIe]{OcV_p@uTwnCej@i[}P_b@yv@{Waa@mb@ma@{l@wp@{^}o@eUui@_`@{nAcZ{yAs\\_pBa^inBcb@{jBm[afCmIwi@{Y{iAke@_~Ai_@mlByQcu@qy@u}Bej@e~A_[kqAwc@eoBos@iqC_g@}mAac@ey@mx@wkBmiAuqC_a@uhAkb@m_B{`@o_C}P}iB{EeaCfBcz@q@kkByOmzBpBcmCdK{dDrDedCeDwbBsI}lAoJmk@kPeh@mYeg@{a@_`@{]aV{JaIkQmP_j@ir@yn@yw@iv@iq@qa@_d@sd@wt@u|Aq`EevA}aCkZg}@{Rqy@al@ubBwi@cdAq`@m`Aoe@wsA{b@o|@_iB}pCq_AmyBmc@w_A_\\kh@izAwcB{`@yb@gaAkpA}pAgyCqf@_~@k{@ekAcv@wqAof@}qAea@_cAy]{p@wcAuaBue@e`A{^qhAwWaqAyL_fAqU_`EdEqeCkFm`@sNcYkN_Km]eB}v@|C{Y{@wq@qAwiArEqr@{F_kAoc@sj@ePwg@}Dkn@|Cg{@fX_fBdw@wv@fRaz@hJs|A}@g\\kDqgAkUokBoa@kt@sFigAl@_rApDaxAaHgs@yKw_AqGqs@h@cp@pF}y@bBeaBAc{AtTaq@rMm{@`Fmt@_EqlAcRut@wB{hAiA_~@gOwyAi`@whCeMkcCoNq{@yO_q@mReoBo}@q{@ua@ak@oPmlEms@iyEyqA}o@oIceAkD_gAu@_cArFuu@fSey@`Ho|@qEku@dDup@nQq]O_\\kQ}Ysj@es@glCmV}bA{NagAsGy`AsOwgAm]msAioAq_Dgt@ipCg_@{eAaXgnAys@_fFiTetAkQwm@mXih@oYu\\s}@q{@iZoa@ucAkpByl@ahAkY{]k^oZaFuKaOg@od@rEqe@xTsSYgYuLgUoC{]l@a\\cCwJjCiXbXaLlQkEnW{Q~r@uSzQ}r@hZyUsCkNoN{m@g}Awm@{|A{I_]|Cuh@vBgf@iDmUwJcMac@oFmLoDuH{HgQwPkQoD_S~AaM~MaPlZmQxAgb@eDuVjDq_Aha@ibAv\\gSuEmPqGuWkOwNcJiB}FaFwLoL|FsDwAsCkKqGeGgF_U|Ry^|Msg@iI{s@aYgg@yTe`@gg@oh@um@gl@kZk^ol@wy@yWk]gJ{d@{D_tBd@wOgGeFyMaG_EiHuCaZkBwQyFwBcFhFq@z@eES}Jb@cDvHwCvM"),
                        distance = 250,
                        dates = setOf(dateFormat.parse("2017-08-12"))
                )
        )
    }()

}