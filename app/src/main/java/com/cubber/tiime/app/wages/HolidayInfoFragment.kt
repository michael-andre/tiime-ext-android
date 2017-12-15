package com.cubber.tiime.app.wages

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.databinding.HolidayInfoFragmentBinding
import com.cubber.tiime.model.Holiday
import com.wapplix.withArguments

/**
 * Created by mike on 15/12/17.
 */
class HolidayInfoFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = HolidayInfoFragmentBinding.inflate(inflater, container, false)

        val holiday = arguments!!.getParcelable<Holiday>(ARG_HOLIDAY)
        binding.holiday = holiday
        binding.editable = arguments!!.getBoolean(ARG_EDITABLE)
        binding.summary = Wages.getShortDatesSummary(holiday.startDate!!, holiday.duration) + " (" +
                getString(R.string.holidays_count_format, holiday.duration / 2f) + ")"

        binding.delete.setOnClickListener {
        }

        return binding.root
    }

    companion object {

        private const val ARG_HOLIDAY = "holiday"
        private const val ARG_EDITABLE = "editable"

        fun newInstance(holiday: Holiday, editable: Boolean) = HolidayInfoFragment().withArguments {
            putParcelable(ARG_HOLIDAY, holiday)
            putBoolean(ARG_EDITABLE, editable)
        }

    }

}