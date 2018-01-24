package com.cubber.tiime.app.wages

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.util.LongSparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.EmployeeWagesFragmentBinding
import com.cubber.tiime.model.Holiday
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.Intents
import com.cubber.tiime.utils.Month
import com.cubber.tiime.utils.bindState
import com.cubber.tiime.utils.showErrorSnackbar
import com.wapplix.arch.*
import com.wapplix.recycler.AutoGridLayoutManager
import com.wapplix.showSnackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class EmployeeWagesFragment : Fragment(), WagesAdapter.Listener {

    private lateinit var vm: VM
    private lateinit var binding: EmployeeWagesFragmentBinding
    private lateinit var layoutManager: AutoGridLayoutManager

    private val employeeId: Long
        get() = arguments!!.getLong(ARG_EMPLOYEE_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = EmployeeWagesFragmentBinding.inflate(inflater, container, false)
        vm = getUiModel()

        layoutManager = AutoGridLayoutManager(context!!, resources.getDimensionPixelSize(R.dimen.handset_card_width))
        val adapter = WagesAdapter(context!!, vm.expandedIds, this)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            DataRepository.of(context!!).wagesUpdate.onNext(emptyList())
        }

        vm.init(employeeId)

        observe(vm.wages) {
            binding.wages = it
            binding.refreshLayout.isRefreshing = false
        }
        observe(vm.wagesSource.state) {
            binding.error.bindState(it)
        }

        return binding.root
    }

    override fun onDateSelected(date: Date, days: Int) {
        val existing = Wages.findHoliday(vm.wages.value!!, date)
        if (existing != null) {
            HolidayInfoFragment.newInstance(existing.second, Wages.isEditable(existing.first))
                    .show(childFragmentManager, "details")
            return
        }
        val targetWage = Wages.getWageForHoliday(vm.wages.value!!, date)
        if (targetWage == null) {
            showSnackbar(R.string.holiday_date_locked, Snackbar.LENGTH_LONG)
            return
        }
        vm.addHoliday(targetWage, date, days * 2)
    }

    override fun onEditComment(item: Wage) {
        WageCommentFragment.newInstance(employeeId, item.id).show(childFragmentManager, "edit_comment")
    }

    override fun onEditIncreaseBonus(item: Wage) {
        WageIncreaseBonusFragment.newInstance(employeeId, item.id).show(childFragmentManager, "edit_increase_bonus")
    }

    override fun onViewAttachment(item: Wage) {
        Intents.startViewActivity(context!!, item.attachment!!)
    }

    class VM(application: Application) : UiModel<EmployeeWagesFragment>(application) {

        private val dataRepository = DataRepository.of(getApplication())

        private var employeeId: Long = 0
        lateinit var wagesSource: StatefulDataSourceFactory<Month, Wage>
        lateinit var wages: LiveData<PagedList<Wage>>
        var expandedIds = LongSparseArray<Boolean>()

        fun init(employeeId: Long) {
            this.employeeId = employeeId
            if (!this@VM::wagesSource.isInitialized) {
                wagesSource = dataRepository.getEmployeeWagesSource(employeeId)
                wages = LivePagedListBuilder(
                        wagesSource,
                        PagedList.Config.Builder()
                                .setPageSize(6)
                                .setEnablePlaceholders(false)
                                .build()
                ).build()
            }
        }

        fun addHoliday(wage: Wage, startDate: Date, duration: Int) {
            onUi {
                HolidayTypePickerFragment.newInstance(startDate, duration, wage.period!!)
                        .show(childFragmentManager, "add_holiday") { type ->
                            if (type == null) return@show
                            val holiday = Holiday(startDate = startDate, duration = duration, type = type)
                            dataRepository.addEmployeeWagesHoliday(employeeId, wage.id, holiday)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            {},
                                            { e -> onUi { showErrorSnackbar(e) } }
                                    )
                        }
            }

        }

        fun deleteHoliday(wageId: Long, holidayId: Long) {
            dataRepository.deleteEmployeeWageHoliday(employeeId, wageId, holidayId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            { e -> onUi { showErrorSnackbar(e) } }
                    )
        }

    }

    companion object {

        private const val ARG_EMPLOYEE_ID = "id"

        fun newInstance(id: Long): EmployeeWagesFragment {
            val args = Bundle()
            args.putLong(ARG_EMPLOYEE_ID, id)
            val fragment = EmployeeWagesFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
