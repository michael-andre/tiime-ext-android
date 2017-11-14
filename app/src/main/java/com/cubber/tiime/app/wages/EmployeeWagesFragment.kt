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
import android.support.v4.util.LongSparseArray
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.WagesSource
import com.cubber.tiime.databinding.EmployeeWagesFragmentBinding
import com.cubber.tiime.model.Wage
import com.wapplix.recycler.AutoGridLayoutManager
import java.util.*

/**
 * Created by mike on 26/10/17.
 */

class EmployeeWagesFragment : Fragment(), WagesAdapter.Listener {

    private lateinit var model: VM
    private lateinit var binding: EmployeeWagesFragmentBinding
    private lateinit var layoutManager: AutoGridLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = EmployeeWagesFragmentBinding.inflate(inflater, container, false)
        model = ViewModelProviders.of(this).get(VM::class.java)

        layoutManager = AutoGridLayoutManager(context!!, resources.getDimensionPixelSize(R.dimen.handset_card_width))
        val adapter = WagesAdapter(context!!, model.expandedIds, this)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        val parent = parentFragment as WagesFragment
        parent.viewYearData.observe(this, Observer { viewYear ->
            layoutManager.columnWidth = resources.getDimensionPixelSize(
                    if (viewYear == true) R.dimen.handset_dual_card_width else R.dimen.handset_card_width
            )
            TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup)
        })

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            model.currentSource?.invalidate()
        }

        val employeeId = arguments!!.getLong(ARG_EMPLOYEE_ID)
        model.init(employeeId)

        model.wages?.observe(this, Observer<PagedList<Wage>> {
            binding.wages = it
            binding.refreshLayout.isRefreshing = false
        })

        return binding.root
    }

    override fun onDateSelected(date: Date) {
        val targetWage = model.wages?.value?.firstEditable(date)
        if (targetWage != null) {
            WageHolidayFragment.newInstance(targetWage.id, date, 2).show(childFragmentManager, "add_holiday")
        } else {
            Snackbar.make(binding.root, R.string.invalid_holiday_date, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onEditComment(item: Wage) {
        WageCommentFragment.newInstance(item.id).show(childFragmentManager, "edit_comment")
    }

    override fun onEditIncreaseBonus(item: Wage) {
        WageIncreaseBonusFragment.newInstance(item.id).show(childFragmentManager, "edit_increase_bonus")
    }

    class VM(application: Application) : AndroidViewModel(application) {

        var wages: LiveData<PagedList<Wage>>? = null
        var expandedIds = LongSparseArray<Boolean>()
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
