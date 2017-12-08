package com.cubber.tiime.app.wages

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.WageCommentDialogBinding
import com.cubber.tiime.model.Wage

/**
 * Created by mike on 07/11/17.
 */
class WageCommentFragment : BottomSheetDialogFragment() {

    private val employeeId : Long
        get() = arguments?.getLong(ARG_EMPLOYEE_ID) ?: throw IllegalArgumentException()
    private val wageId : Long
        get() = arguments?.getLong(ARG_WAGE_ID) ?: throw IllegalArgumentException()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val model = ViewModelProviders.of(this).get(VM::class.java)
        model.employeeId = employeeId
        model.wageId = wageId

        val binding = WageCommentDialogBinding.inflate(inflater, container, false)
        binding.toolbar.inflateMenu(R.menu.wage_comment)
        binding.toolbar.setOnMenuItemClickListener { when (it.itemId) {
            R.id.save -> {
                saveWage()
                true
            }
            else -> false
        } }
        model.wage.observe(this, Observer { wage ->
            binding.wage = wage
        })
        return binding.root
    }

    private fun saveWage() {

    }

    class VM(app: Application) : AndroidViewModel(app) {

        var wageId : Long = 0
        var employeeId : Long = 0
        val wage: LiveData<Wage> by lazy {
            DataRepository.of(getApplication()).wage(employeeId, wageId)
        }

    }

    companion object {

        private const val ARG_EMPLOYEE_ID = "employee_id"
        private const val ARG_WAGE_ID = "wage_id"

        fun newInstance(employeeId: Long, wageId: Long): WageCommentFragment {
            return WageCommentFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EMPLOYEE_ID, employeeId)
                    putLong(ARG_WAGE_ID, wageId)
                }
            }
        }
    }

}