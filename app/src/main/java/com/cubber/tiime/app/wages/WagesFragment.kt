package com.cubber.tiime.app.wages

import android.app.Application
import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.WagesFragmentBinding
import com.cubber.tiime.model.Employee



/**
 * Created by mike on 21/09/17.
 */

class WagesFragment : Fragment() {

    private lateinit var model: VM

    val viewYearData: LiveData<Boolean>
        get() = model.viewYear

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val b = WagesFragmentBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(b.toolbar)
        b.toolbar.setTitle(R.string.wages)

        val adapter = Adapter(childFragmentManager)
        b.pager.adapter = adapter
        b.tabs.setupWithViewPager(b.pager)

        model = ViewModelProviders.of(this).get(VM::class.java)
        model.employees.observe(this, Observer { adapter.setEmployees(it) })

        return b.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.wages, menu)
        val yearItem = menu!!.findItem(R.id.view_year)
        model.viewYear.observe(this, Observer { viewYear ->
            yearItem.isChecked = viewYear == true
            yearItem.icon = ContextCompat.getDrawable(context!!, if (viewYear == true) R.drawable.ic_view_cards else R.drawable.ic_view_year)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.view_year -> { model.viewYear.postValue(!item.isChecked); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class Adapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        private var employees: List<Employee>? = null
        private val indicatorSpan by lazy { ImageSpan(context, R.drawable.indicator_action_required_small, ImageSpan.ALIGN_BASELINE) }

        fun setEmployees(employees: List<Employee>?) {
            this.employees = employees
            notifyDataSetChanged()
        }

        override fun getCount(): Int = if (employees == null) 0 else employees!!.size

        override fun getItem(position: Int): Fragment = EmployeeWagesFragment.newInstance(employees!![position].id)

        override fun getPageTitle(position: Int): CharSequence? {
            val employee = employees!![position]
            val builder = SpannableStringBuilder()
            builder.append(employee.name)
            if (employee.wagesValidationRequired) {
                builder.append(" *")
                builder.setSpan(indicatorSpan, builder.length - 1, builder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            return builder
        }

    }

    class VM(application: Application) : AndroidViewModel(application) {

        var employees = DataRepository.of(getApplication()).employees()
        var viewYear = MutableLiveData<Boolean>()

        init {
            viewYear.value = false
        }

    }

}
