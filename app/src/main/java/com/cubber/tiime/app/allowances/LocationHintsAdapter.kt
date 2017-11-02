package com.cubber.tiime.app.allowances

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.cubber.tiime.R
import com.cubber.tiime.model.Client
import com.cubber.tiime.utils.filterCleaned
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePredictionBuffer
import com.google.android.gms.location.places.PlaceLikelihoodBuffer
import com.google.android.gms.location.places.Places
import com.google.common.base.Optional
import com.wapplix.arch.EventData
import com.wapplix.gms.PendingResultData
import com.wapplix.widget.ListAdapter
import java.util.*

/**
 * Created by mike on 28/09/17.
 */

class LocationHintsAdapter(client: LiveData<GoogleApiClient>, lifecycleOwner: LifecycleOwner) : ListAdapter<LocationHintsAdapter.Item>(), Filterable {

    private val currentLocation = Item("", null)
    private val filter = HintsFilter()
    private val mQuery = MutableLiveData<String>()

    private var clients: List<Item>? = null
    private var officeAddress: Item? = null
    private var currentPlaces: List<Item>? = null
    private var autocompletePredictions: List<Item>? = null

    init {
        val autocompleteFilter = AutocompleteFilter.Builder()
                .setCountry("FR")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build()
        Transformations.switchMap<GoogleApiClient, AutocompletePredictionBuffer>(client) { c ->
            PendingResultData.cancellingSwitchMap<String, AutocompletePredictionBuffer>(mQuery
            ) { query -> Places.GeoDataApi.getAutocompletePredictions(c, query, null, autocompleteFilter) }
        }.observe(lifecycleOwner, Observer{ buffer ->
            if (buffer?.status?.isSuccess == true) {
                autocompletePredictions = buffer.map { Item(
                        address = it.getFullText(null).toString(),
                        placeId = it.placeId
                ) }
                filter.notifySourceDataChanged()
            }
            buffer?.release()
        })
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.support_simple_spinner_dropdown_item, parent, false)
    }

    override fun onBindView(view: View, item: Item) {
        val tv = view.findViewById<TextView>(android.R.id.text1)
        if (currentLocation === item) {
            tv.setText(R.string.current_location)
        } else {
            tv.text = filter.convertResultToString(item)
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    fun getItemListener(currentPlaceTrigger: EventData<*>): AdapterView.OnItemClickListener {
        return AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            if (adapterView.adapter == this@LocationHintsAdapter && currentLocation === getItem(position)) {
                currentPlaceTrigger.trigger()
            }
        }
    }

    fun setClients(clients: List<Client>?) {
        this.clients = clients?.map { Item(
                address = it.name + ", " + it.address
        )}
        filter.notifySourceDataChanged()
    }

    fun setCurrentPlaces(places: PlaceLikelihoodBuffer?) {
        currentPlaces = places?.map { Item(
                address = if (it.place.address.startsWith(it.place.name)) {
                    it.place.address.toString()
                } else {
                    it.place.name.toString() + ", " + it.place.address
                },
                placeId = it.place.id
        ) }
        places?.release()
        filter.notifySourceDataChanged()
    }

    fun setOfficeAddress(officeAddress: Optional<String>) {
        this.officeAddress = if (officeAddress.isPresent) Item(
                address = officeAddress.get()
        ) else null
        filter.notifySourceDataChanged()
    }

    private inner class HintsFilter : Filter() {

        private var lastQuery: CharSequence? = null

        fun notifySourceDataChanged() {
            filter(lastQuery)
        }

        override fun performFiltering(query: CharSequence?): Filter.FilterResults {
            lastQuery = query
            val results = Filter.FilterResults()
            if (query == null) return results
            val values = ArrayList<Item>()
            val currentPlaces = this@LocationHintsAdapter.currentPlaces
            // Always show "Current location"
            if (currentPlaces == null) {
                values.add(currentLocation)
            }
            // Show current places if loaded & query is empty (max 3)
            if (currentPlaces != null && query.isEmpty()) {
                values.addAll(if (currentPlaces.size > 3) currentPlaces.subList(0, 3) else currentPlaces)
            }
            // Always show office address if loaded & defined
            val officeAddress = this@LocationHintsAdapter.officeAddress
            if (officeAddress != null) {
                values.add(officeAddress)
            }
            // Show matching clients from 1 character
            val clients = this@LocationHintsAdapter.clients
            if (query.isNotEmpty() && clients != null) {
                values.addAll(clients.filterCleaned(query) { c -> arrayOf(c.address) })
            }
            // Show autocomplete suggestions from 2 characters
            if (query.length > 2) {
                mQuery.postValue(query.toString())
                if (autocompletePredictions != null) {
                    values.addAll(autocompletePredictions!!)
                }
            }
            results.count = values.size
            results.values = values
            return results
        }

        override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults?) {
            @Suppress("unchecked_cast")
            items =  filterResults?.values as List<Item>?
        }

        override fun convertResultToString(item: Any): CharSequence? {
            if (currentLocation === item) {
                return null
            } else if (item is Item) {
                return item.address
            }
            return super.convertResultToString(item)
        }

    }

    data class Item(var address: String, var placeId: String? = null)

}
