package com.cubber.tiime.app.mileages

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.widget.Toast
import com.cubber.tiime.R
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.wapplix.arch.ResultEmitter
import com.wapplix.arch.result
import java.util.*

/**
 * Created by mike on 27/09/17.
 */

class DatesPickerFragment : AppCompatDialogFragment(), ResultEmitter<Collection<Date>>, DialogInterface.OnShowListener {

    private lateinit var calendarView: MaterialCalendarView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        @Suppress("unchecked_cast")
        val selection = arguments?.getSerializable(ARG_DATES) as ArrayList<Date>?

        calendarView = MaterialCalendarView(context!!)
        calendarView.setHeaderTextAppearance(android.support.design.R.style.TextAppearance_AppCompat_Widget_Button)
        val padding = resources.getDimensionPixelSize(R.dimen.spacing)
        calendarView.setPadding(padding, 0, padding,0)
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE
        val cal = Calendar.getInstance()
        val sb = calendarView.state().edit()
        sb.setMaximumDate(cal)
        cal.add(Calendar.YEAR, -1)
        sb.setMinimumDate(cal)
        sb.commit()
        if (selection != null) {
            for (d in selection) calendarView.setDateSelected(d, true)
        }

        val dialog = AlertDialog.Builder(context!!)
                .setTitle(R.string.trip_dates)
                .setView(calendarView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .create()
        dialog.setOnShowListener(this)
        return dialog
    }

    override fun onShow(dialog: DialogInterface) {
        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val dates = calendarView.selectedDates.map { it.date }
            if (dates.isEmpty()) {
                Toast.makeText(context, R.string.dates_empty_error, Toast.LENGTH_LONG).show()
            } else {
                result.onResult(dates)
                dismiss()
            }
        }
    }

    companion object {

        private val ARG_DATES = "dates"

        fun newInstance(selection: Collection<Date>?): DatesPickerFragment {
            val args = Bundle()
            if (selection != null) {
                args.putSerializable(ARG_DATES, ArrayList(selection))
            }
            val fragment = DatesPickerFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
