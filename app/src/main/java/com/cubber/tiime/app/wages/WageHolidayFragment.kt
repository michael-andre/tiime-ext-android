package com.cubber.tiime.app.wages

import android.app.Dialog
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
import com.wapplix.recycler.BindingListAdapter
import java.util.*

/**
 * Created by mike on 30/10/17.
 */

class WageHolidayFragment : BottomSheetDialogFragment() {

    lateinit var binding: WageHolidayDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WageHolidayDialogBinding.inflate(inflater, container, false)

        binding.type.layoutManager = GridLayoutManager(context, 3)
        binding.type.adapter = Adapter()

        val startDate = arguments!!.getSerializable(ARG_START_DATE) as Date
        val duration = arguments!!.getInt(ARG_DURATION)
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