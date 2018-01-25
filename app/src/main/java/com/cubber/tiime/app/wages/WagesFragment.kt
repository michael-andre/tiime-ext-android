package com.cubber.tiime.app.wages

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.WagesFragmentBinding
import com.cubber.tiime.model.Employee
import com.cubber.tiime.utils.bindState
import com.wapplix.arch.*
import com.wapplix.pager.FragmentStateListAdapter


/**
 * Created by mike on 21/09/17.
 */

class WagesFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = WagesFragmentBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitle(R.string.wages)

        val adapter = Adapter(childFragmentManager)
        binding.pager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.pager)

        val vm: VM = getUiModel()
        observe(vm.employees) {
            binding.employees = it
        }
        observe(vm.employees.state) {
            binding.error.bindState(it)
        }

        return binding.root
    }

    private inner class Adapter(fm: FragmentManager) : FragmentStateListAdapter<Employee>(fm) {

        private val indicatorSpan by lazy {
            ImageSpan(context, R.drawable.indicator_action_required_small, ImageSpan.ALIGN_BASELINE)
        }

        override fun onCreateFragment(item: Employee)
                = EmployeeWagesFragment.newInstance(item.id)

        override fun getPageTitle(position: Int): CharSequence? {
            val employee = items!![position]
            val builder = SpannableStringBuilder()
            builder.append(employee.name)
            if (employee.wagesValidationRequired) {
                builder.append(" *")
                builder.setSpan(indicatorSpan, builder.length - 1, builder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            return builder
        }

    }

    class VM(application: Application) : UiModel<WagesFragment>(application) {

        var employees = DataRepository.of(getApplication()).employees().toStatefulLiveData()

    }

}
