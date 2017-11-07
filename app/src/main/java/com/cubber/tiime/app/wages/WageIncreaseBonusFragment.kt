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
import android.widget.TextView
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.WageIncreaseBonusDialogBinding
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.euroFormat
import com.wapplix.widget.SimpleAdapter

/**
 * Created by mike on 07/11/17.
 */
class WageIncreaseBonusFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val model = ViewModelProviders.of(this).get(VM::class.java)
        model.wageId = arguments?.getLong(ARG_WAGE_ID) ?: throw IllegalArgumentException()

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
        val typeAdapter = object : SimpleAdapter<String>(context!!, android.R.layout.simple_spinner_item, R.layout.support_simple_spinner_dropdown_item) {
            override fun onBindView(view: View, @Wage.SalaryType item: String) {
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.setText(if (item == Wage.SALARY_TYPE_NET) R.string.net else R.string.gross)
            }
        }
        typeAdapter.items = listOf(Wage.SALARY_TYPE_NET, Wage.SALARY_TYPE_GROSS)
        binding.increase.applyFormat(euroFormat())
        binding.increaseType.setAdapter(typeAdapter)
        binding.bonus.applyFormat(euroFormat())
        binding.bonusType.setAdapter(typeAdapter)
        model.wage.observe(this, Observer { wage ->
            binding.wage = wage
        })
        return binding.root
    }

    private fun saveWage() {

    }

    class VM(app: Application) : AndroidViewModel(app) {

        var wageId: Long = 0
        val wage: LiveData<Wage?> by lazy {
            DataRepository.of(getApplication()).wage(wageId)
        }

    }

    companion object {

        private const val ARG_WAGE_ID = "wage_id"

        fun newInstance(wageId: Long): WageIncreaseBonusFragment {
            return WageIncreaseBonusFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_WAGE_ID, wageId)
                }
            }
        }
    }

}