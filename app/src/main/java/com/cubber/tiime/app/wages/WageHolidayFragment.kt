package com.cubber.tiime.app.wages

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.databinding.WageHolidayDialogBinding
import com.cubber.tiime.databinding.WageHolidayDialogItemBinding
import com.cubber.tiime.model.Holiday
import com.wapplix.recycler.BindingListAdapter
import java.util.*

/**
 * Created by mike on 30/10/17.
 */

class WageHolidayFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = WageHolidayDialogBinding.inflate(inflater, container, false)

        binding.type.adapter = Adapter()

        val startDate = arguments!!.getSerializable(ARG_START_DATE) as Date
        val duration = arguments!!.getInt(ARG_DURATION)
        binding.duration = duration
        binding.toolbar.title = Wages.getShortDatesSummary(startDate, duration)

        return binding.root
    }

    private fun addHoliday(@Holiday.Type type: String) {
        dismiss()
    }

    private inner class Adapter : BindingListAdapter<String, WageHolidayDialogItemBinding>() {
        init {
            items = Holiday.TYPES.asList()
        }

        override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): WageHolidayDialogItemBinding {
            return WageHolidayDialogItemBinding.inflate(inflater, parent, false)
        }

        override fun onBindView(binding: WageHolidayDialogItemBinding, item: String) {
            binding.type = item
            binding.root.setOnClickListener { addHoliday(item) }
        }

    }

    companion object {

        private const val ARG_WAGE_ID = "wage_id"
        private const val ARG_START_DATE = "start_date"
        private const val ARG_DURATION = "duration"

        fun newInstance(wageId: Long, startDate: Date, duration: Int): WageHolidayFragment {
            return WageHolidayFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_WAGE_ID, wageId)
                    putSerializable(ARG_START_DATE, startDate)
                    putInt(ARG_DURATION, duration)
                }
            }
        }
    }

}