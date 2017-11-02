package com.cubber.tiime.app.wages

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListProvider
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.WagesSource
import com.cubber.tiime.databinding.EmployeeWagesFragmentBinding
import com.cubber.tiime.model.Wage
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class EmployeeWagesFragment : Fragment(), OnDateSelectedListener {

    private var layoutManager: GridLayoutManager? = null
    private lateinit var model: VM
    private lateinit var binding: EmployeeWagesFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = EmployeeWagesFragmentBinding.inflate(inflater, container, false)

        layoutManager = GridLayoutManager(context, 1)
        val adapter = WagesAdapter(context!!, this)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        val parent = parentFragment as WagesFragment
        parent.showYearViewData.observe(this, Observer { showYearView ->
            layoutManager!!.spanCount = if (showYearView == true) 2 else 1
            adapter.setShowYearView(showYearView == true)
            TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup)
        })

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            model.currentSource?.invalidate()
        }

        model = ViewModelProviders.of(this).get(VM::class.java)
        val employeeId = arguments!!.getLong(ARG_EMPLOYEE_ID)
        model.init(employeeId)

        model.wages?.observe(this, Observer<PagedList<Wage>> {
            binding.wages = it
            binding.refreshLayout.isRefreshing = false
        })

        return binding.root
    }

    override fun onDateSelected(view: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        view.setDateSelected(date, false)
        val startDate = date.date
        val targetWage = model.wages?.value?.firstEditable(startDate)
        if (targetWage != null) {
            AddHolidayFragment.newInstance(targetWage.id, startDate, 2).show(childFragmentManager, "add_holiday")
        } else {
            Snackbar.make(getView()!!, R.string.invalid_holiday_date, Snackbar.LENGTH_LONG).show()
        }
    }

    class VM(application: Application) : AndroidViewModel(application) {

        var wages: LiveData<PagedList<Wage>>? = null
        var currentSource: WagesSource? = null

        fun init(employeeId: Long) {
            if (wages == null) {
                wages = object : LivePagedListProvider<Date, Wage>() {

                    override fun createDataSource(): DataSource<Date, Wage> {
                        val source = WagesSource(getApplication(), employeeId)
                        currentSource = source
                        return source
                    }

                }.create(null, 6)
            }
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
