package com.cubber.tiime.app.wages

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.WageIncreaseBonusDialogBinding
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.euroFormat
import com.wapplix.bundleOf
import com.wapplix.widget.SimpleAdapter

/**
 * Created by mike on 07/11/17.
 */
class WageIncreaseBonusFragment : BottomSheetDialogFragment() {

    private val employeeId : Long
        get() = arguments?.getLong(ARG_EMPLOYEE_ID) ?: throw IllegalArgumentException()
    private val wageId : Long
        get() = arguments?.getLong(ARG_WAGE_ID) ?: throw IllegalArgumentException()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val model = ViewModelProviders.of(this).get(VM::class.java)
        model.employeeId = employeeId
        model.wageId = wageId

        val binding = WageIncreaseBonusDialogBinding.inflate(inflater, container, false)
        binding.toolbar.inflateMenu(R.menu.wage_comment)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> {
                    saveWage()
                    true
                }
                else -> false
            }
        }
        val typeAdapter = SimpleAdapter<String>(context!!, { getString(if (it == Wage.SALARY_TYPE_NET) R.string.net else R.string.gross) })
        typeAdapter.items = listOf(Wage.SALARY_TYPE_NET, Wage.SALARY_TYPE_GROSS)
        binding.increase.applyFormat(euroFormat())
        binding.increaseType.adapter = typeAdapter
        binding.bonus.applyFormat(euroFormat())
        binding.bonusType.adapter = typeAdapter
        model.wage.observe(this, Observer { wage ->
            binding.wage = wage
        })
        return binding.root
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val behavior = BottomSheetBehavior.from((it as BottomSheetDialog).findViewById<View>(android.support.design.R.id.design_bottom_sheet))
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun saveWage() {

    }

    class VM(app: Application) : AndroidViewModel(app) {

        var wageId: Long = 0
        var employeeId : Long = 0
        val wage: LiveData<Wage> by lazy {
            DataRepository.of(getApplication()).wage(employeeId, wageId)
        }

    }

    companion object {

        private const val ARG_EMPLOYEE_ID = "employee_id"
        private const val ARG_WAGE_ID = "wage_id"

        fun newInstance(employeeId: Long, wageId: Long): WageIncreaseBonusFragment {
            return WageIncreaseBonusFragment().apply {
                arguments = bundleOf {
                    putLong(ARG_EMPLOYEE_ID, employeeId)
                    putLong(ARG_WAGE_ID, wageId)
                }
            }
        }
    }

}