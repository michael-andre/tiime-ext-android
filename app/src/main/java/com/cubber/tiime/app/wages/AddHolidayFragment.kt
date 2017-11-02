package com.cubber.tiime.app.wages

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.databinding.AddHolidayDialogBinding
import com.cubber.tiime.databinding.AddHolidayDialogItemBinding
import com.cubber.tiime.model.Holiday
import com.wapplix.recycler.BindingListAdapter
import java.util.*

/**
 * Created by mike on 30/10/17.
 */

class AddHolidayFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AddHolidayDialogBinding.inflate(inflater, container, false)

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

    private inner class Adapter : BindingListAdapter<String, AddHolidayDialogItemBinding>() {
        init {
            items = Arrays.asList(*Holiday.TYPES)
        }

        override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): AddHolidayDialogItemBinding {
            return AddHolidayDialogItemBinding.inflate(inflater, parent, false)
        }

        override fun onBindView(binding: AddHolidayDialogItemBinding, item: String) {
            binding.type = item
            binding.root.setOnClickListener { addHoliday(item) }
        }

    }

    companion object {

        private const val ARG_WAGE_ID = "wage_id"
        private const val ARG_START_DATE = "start_date"
        private const val ARG_DURATION = "duration"

        fun newInstance(wageId: Long, startDate: Date, duration: Int): AddHolidayFragment {
            val args = Bundle()
            args.putLong(ARG_WAGE_ID, wageId)
            args.putSerializable(ARG_START_DATE, startDate)
            args.putInt(ARG_DURATION, duration)
            val fragment = AddHolidayFragment()
            fragment.arguments = args
            return fragment
        }
    }

}