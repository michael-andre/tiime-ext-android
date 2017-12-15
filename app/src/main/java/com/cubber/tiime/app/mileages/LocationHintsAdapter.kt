package com.cubber.tiime.app.mileages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.cubber.tiime.R
import com.cubber.tiime.model.Client
import com.cubber.tiime.utils.filterCleaned
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Place
import com.wapplix.widget.ListAdapter
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by mike on 28/09/17.
 */

class LocationHintsAdapter(client: GeoDataClient) : ListAdapter<LocationHintsAdapter.Item>(), Filterable {

    private val currentLocation = Item("", null)
    private val filter = HintsFilter()
    private val query = PublishSubject.create<String>()

    private var clients: List<Item>? = null
    private var officeAddress: Item? = null
    private var currentPlaces: List<Item>? = null
    private var autocompletePredictions: List<Item>? = null

    var currentPlacesLoading = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        val autocompleteFilter = AutocompleteFilter.Builder()
                .setCountry("FR")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build()
        query.debounce(1, TimeUnit.SECONDS)
                .switchMapSingle { q ->
                    Single.create<List<Item>> { e ->
                        client.getAutocompletePredictions(q, null, autocompleteFilter)
                                .addOnSuccessListener { b ->
                                    e.onSuccess(b.map {
                                        Item(address = it.getFullText(null).toString(), placeId = it.placeId)
                                    })
                                    b.release()
                                }
                    }
                }
                .subscribe { p ->
                    autocompletePredictions = p
                    filter.notifySourceDataChanged()
                }
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): View
            = LayoutInflater.from(parent.context)
            .inflate(R.layout.support_simple_spinner_dropdown_item, parent, false)

    override fun onBindView(view: View, item: Item) {
        val tv = view.findViewById<TextView>(android.R.id.text1)
        if (currentLocation === item) {
            tv.setText(R.string.current_location)
        } else {
            tv.text = filter.convertResultToString(item)
        }
    }

    override fun areAllItemsEnabled() = !currentPlacesLoading

    override fun isEnabled(position: Int): Boolean =
            if (getItem(position) === currentLocation) !currentPlacesLoading else true

    fun isCurrentLocationItemPosition(position: Int) = getItem(position) === currentLocation

    override fun getFilter(): Filter {
        return filter
    }

    fun setClients(clients: List<Client>?) {
        this.clients = clients?.map {
            Item(
                    address = it.name + ", " + it.directionsAddress
            )
        }
        filter.notifySourceDataChanged()
    }

    fun setCurrentPlaces(places: List<Item>?) {
        currentPlaces = places
        filter.notifySourceDataChanged()
    }

    fun setOfficeAddress(address: String?) {
        this.officeAddress = if (address != null) Item(address) else null
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
            // Always show "Current location", if not already loaded
            if (currentPlaces == null) {
                values.add(currentLocation)
            }
            // Show 3 current places if loaded & query is empty
            if (currentPlaces != null && query.isEmpty()) {
                values.addAll(currentPlaces.take(3))
            }
            // Always show office address if loaded & defined
            val officeAddress = this@LocationHintsAdapter.officeAddress
            if (officeAddress != null) {
                values.add(officeAddress)
            }
            // Show matching clients from 1 character
            val clients = this@LocationHintsAdapter.clients
            if (query.isNotEmpty() && clients != null) {
                values.addAll(clients.filterCleaned(query) { c -> sequenceOf(c.address) })
            }
            // Show autocomplete suggestions from 2 characters
            if (query.length > 2) {
                this@LocationHintsAdapter.query.onNext(query.toString())
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
            items = filterResults?.values as List<Item>?
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

    data class Item(var address: String, var placeId: String? = null) {

        constructor(place: Place) : this(
                address = if (place.address.startsWith(place.name)) {
                    place.address.toString()
                } else {
                    place.name.toString() + ", " + place.address
                },
                placeId = place.id
        )

    }

}
