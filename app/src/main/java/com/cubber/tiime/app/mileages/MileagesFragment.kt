package com.cubber.tiime.app.mileages

import android.app.Application
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.data.PolylineService
import com.cubber.tiime.databinding.MileagesFragmentBinding
import com.wapplix.arch.UiModel
import com.wapplix.arch.getUiModel
import com.wapplix.arch.observe

/**
 * Created by mike on 21/09/17.
 */

class MileagesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = MileagesFragmentBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitle(R.string.mileage_allowances)
        val adapter = MileagesAdapter(context!!)
        binding.list.adapter = adapter
        binding.fab.setOnClickListener { startActivity(MileagesActivity.newIntent(context!!)) }

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            DataRepository.of(context!!).allowancesUpdate.onNext(emptyList())
        }

        val vm: VM = getUiModel()
        observe(vm.mileages) {
            binding.mileages = it
            binding.refreshLayout.isRefreshing = false
        }
        observe(PolylineService.getInstance(context!!).loadingPolylines) {
            adapter.loadingPolylines = it
        }

        return binding.root
    }

    class VM(application: Application) : UiModel<MileagesFragment>(application) {

        private val dataRepository = DataRepository.of(getApplication())

        internal var mileages = LivePagedListBuilder(
                dataRepository.getMileageAllowances(),
                PagedList.Config.Builder()
                        .setPageSize(20)
                        .setEnablePlaceholders(false)
                        .build()
        ).build()

    }

}
