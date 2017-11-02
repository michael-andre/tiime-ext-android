package com.cubber.tiime.app.allowances

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.MileageAllowancesSource
import com.cubber.tiime.databinding.AllowancesFragmentBinding
import com.cubber.tiime.model.MileageAllowance

/**
 * Created by mike on 21/09/17.
 */

class AllowancesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val b = AllowancesFragmentBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(b.toolbar)
        b.toolbar.setTitle(R.string.mileage_allowances)
        b.list.adapter = AllowancesAdapter()
        b.fab.setOnClickListener { startActivity(AllowanceActivity.newIntent(context!!)) }

        val vm = ViewModelProviders.of(this).get(VM::class.java)
        vm.allowances.observe(this, Observer { b.allowances = it })

        return b.root
    }

    class VM(application: Application) : AndroidViewModel(application) {

        internal var allowances = object : LivePagedListProvider<Int, MileageAllowance>() {

            override fun createDataSource(): DataSource<Int, MileageAllowance> {
                return MileageAllowancesSource(application)
            }

        }.create(null, 20)

    }

}
