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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val model = ViewModelProviders.of(this).get(VM::class.java)
        model.wageId = arguments?.getLong(ARG_WAGE_ID) ?: throw IllegalArgumentException()

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
        val wage: LiveData<Wage?> by lazy {
            DataRepository.of(getApplication()).wage(wageId)
        }

    }

    companion object {

        private const val ARG_WAGE_ID = "wage_id"

        fun newInstance(wageId: Long): WageCommentFragment {
            return WageCommentFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_WAGE_ID, wageId)
                }
            }
        }
    }

}