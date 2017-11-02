package com.cubber.tiime.app.allowances

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cubber.tiime.databinding.AllowanceItemBinding
import com.cubber.tiime.model.MileageAllowance
import com.wapplix.recycler.BindingPagedListAdapter

/**
 * Created by mike on 21/09/17.
 */
class AllowancesAdapter : BindingPagedListAdapter<MileageAllowance, AllowanceItemBinding>({ id }) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): AllowanceItemBinding {
        val binding = AllowanceItemBinding.inflate(inflater, parent, false)
        binding.addOnRebindCallback(object : AllowanceBindingCallback<AllowanceItemBinding>() {
            override fun onBound(binding: AllowanceItemBinding?) {
                super.onBound(binding)
                setPolyline(binding!!.map, binding.allowance?.polyline)
            }
        })
        binding.map.onCreate(null)
        binding.map.getMapAsync { map ->
            map.setOnMapClickListener { }
            map.uiSettings.isMapToolbarEnabled = false
        }
        return binding
    }

    override fun onBindView(binding: AllowanceItemBinding, item: MileageAllowance) {
        binding.allowance = item
    }

    override fun getItemId(position: Int): Long {
        val e = getItem(position)
        return e?.id ?: RecyclerView.NO_ID
    }

}
