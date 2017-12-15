package com.cubber.tiime.app.wages

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.databinding.WageHolidayDialogBinding
import com.cubber.tiime.databinding.WageHolidayDialogItemBinding
import com.cubber.tiime.model.Holiday
import com.wapplix.arch.ResultEmitter
import com.wapplix.arch.result
import com.wapplix.recycler.BindingListAdapter
import com.wapplix.withArguments
import java.util.*

/**
 * Created by mike on 30/10/17.
 */

class HolidayTypePickerFragment : BottomSheetDialogFragment(), ResultEmitter<String?> {

    lateinit var binding: WageHolidayDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WageHolidayDialogBinding.inflate(inflater, container, false)

        binding.type.layoutManager = GridLayoutManager(context, 3)
        binding.type.adapter = Adapter()

        val startDate = arguments!!.getSerializable(ARG_START_DATE) as Date
        val duration = arguments!!.getInt(ARG_DURATION)

        val wagePeriod = arguments!!.getSerializable(ARG_WAGE_PERIOD) as Date
        val cal = Calendar.getInstance()
        cal.clear()
        cal.time = startDate
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        if (cal.time != wagePeriod) {
            binding.wagePeriodWarning = wagePeriod
        }

        binding.duration = duration
        binding.toolbar.title = Wages.getShortDatesSummary(startDate, duration)

        return binding.root
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnShowListener {
            val behavior = BottomSheetBehavior.from((it as BottomSheetDialog).findViewById<View>(android.support.design.R.id.design_bottom_sheet))
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun addHoliday(@Holiday.Type type: String) {
        result.onResult(type)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        result.onResult(null)
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

        private const val ARG_START_DATE = "start_date"
        private const val ARG_DURATION = "duration"
        private const val ARG_WAGE_PERIOD = "period"

        fun newInstance(startDate: Date, duration: Int, wagePeriod: Date): HolidayTypePickerFragment {
            return HolidayTypePickerFragment().withArguments {
                putSerializable(ARG_START_DATE, startDate)
                putInt(ARG_DURATION, duration)
                putSerializable(ARG_WAGE_PERIOD, wagePeriod)
            }
        }
    }

}