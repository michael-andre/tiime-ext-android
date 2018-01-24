package com.cubber.tiime.app.mileages

import android.content.Context
import android.databinding.OnRebindCallback
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cubber.tiime.data.PolylineService
import com.cubber.tiime.databinding.MileageItemBinding
import com.cubber.tiime.model.MileageAllowance
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.common.collect.Sets
import com.wapplix.arch.paging.diffCallbackBy
import com.wapplix.recycler.BindingPagedListAdapter

/**
 * Created by mike on 21/09/17.
 */
class MileagesAdapter(context: Context) : BindingPagedListAdapter<MileageAllowance, MileageItemBinding>(diffCallbackBy { id }) {

    internal var mapOptionsCache = mutableMapOf<List<LatLng>, PolylineMapOptions>()
    internal var mapOptionsFactory = PolylineMapOptionsFactory(context)
    private var polylineService = PolylineService.getInstance(context)

    var loadingPolylines: Set<Long>? = null
    set(value) {
        val diff = Sets.symmetricDifference(field.orEmpty(), value.orEmpty()).toSet()
        field = value?.toSet()
        for (id in diff) {
            val pos = currentList?.indexOfFirst { it.id == id } ?: -1
            if (pos > -1) notifyItemChanged(pos)
        }
    }

    init {
        setHasStableIds(true)
    }

    private val mapCallback = OnMapReadyCallback { map ->
        map.setOnMapClickListener { }
        map.uiSettings.isMapToolbarEnabled = false
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): MileageItemBinding {
        val binding = MileageItemBinding.inflate(inflater, parent, false)
        binding.addOnRebindCallback(object: OnRebindCallback<MileageItemBinding>() {

            private val mapHelper = PolylineMapHelper(binding.map)

            override fun onBound(binding: MileageItemBinding) {
                val polyline = binding.mileageAllowance?.polyline
                val options = if (polyline != null)
                    mapOptionsCache.getOrPut(polyline) { mapOptionsFactory.create(polyline) }
                else null
                mapHelper.applyOptions(options)
            }

        })
        binding.map.onCreate(null)
        binding.map.getMapAsync(mapCallback)
        return binding
    }

    override fun onBindView(binding: MileageItemBinding, item: MileageAllowance) {
        binding.mileageAllowance = item
        binding.polylineLoading = loadingPolylines.orEmpty().contains(item.id)
        if (item.polyline == null && !loadingPolylines.orEmpty().contains(item.id)) {
            polylineService.loadPolyline(item)
        }
    }

    override fun getItemId(position: Int): Long =
            getItem(position)?.id ?: RecyclerView.NO_ID

}