package com.cubber.tiime.app.mileages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import com.cubber.tiime.R
import com.cubber.tiime.app.mileages.vehicles.VehiclePickerFragment
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.AllowanceActivityBinding
import com.cubber.tiime.model.Client
import com.cubber.tiime.model.MileageAllowance
import com.cubber.tiime.model.Vehicle
import com.cubber.tiime.utils.ArchDialogs
import com.cubber.tiime.utils.Views
import com.cubber.tiime.utils.fullDateFormat
import com.google.android.gms.location.places.PlaceLikelihoodBuffer
import com.google.android.gms.location.places.Places
import com.google.common.base.Optional
import com.google.maps.DirectionsApi
import com.google.maps.model.EncodedPolyline
import com.google.maps.model.TravelMode
import com.wapplix.arch.*
import com.wapplix.gms.GoogleApiClientData
import com.wapplix.gms.toData
import com.wapplix.maps.GeoUtils
import com.wapplix.maps.toData
import java.util.*
import kotlin.collections.HashSet

/**
 * An activity to create and save a new [MileageAllowance] item.
 */
class AllowanceActivity : AppCompatActivity() {

    private lateinit var vm: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProviders.of(this).get(VM::class.java)

        val b = DataBindingUtil.setContentView<AllowanceActivityBinding>(this, R.layout.allowance_activity)
        b.addOnRebindCallback(object : AllowanceBindingCallback<AllowanceActivityBinding>() {

            override fun onBound(binding: AllowanceActivityBinding?) {
                setPolyline(b.map, b.allowance?.polyline)
            }

        })

        // Action bar
        setSupportActionBar(b.toolbar)
        with(supportActionBar!!) {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_close_24dp)
        }

        // Hints
        val hintsAdapter = AllowanceHintAdapter()
        b.hints.adapter = hintsAdapter
        b.reason.addTextChangedListener(hintsAdapter.getQueryWatcher(b.reason))
        b.reason.setOnFocusChangeListener { _, hasFocus ->
            b.hints.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
        b.reason.setOnEditorActionListener { textView, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL -> {
                    if (TextUtils.isEmpty(b.distance.text)) {
                        b.distance.requestFocus()
                    } else {
                        Views.hideSoftInput(textView)
                    }
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) b.reason.requestFocus()
        hintsAdapter.onClientHintClick = { client ->
            vm.setClient(client)
            expandAddresses(b)
            b.startingAddress.requestFocus()
        }

        // Vehicle
        b.vehicleLayout.setOnClickListener { VehiclePickerFragment().showForResult(this, "vehicle_picker") }
        ArchDialogs.resultOf(VehiclePickerFragment::class.java, "vehicle_picker", this)
                .observe(this, Observer { vehicle ->
                    if (vehicle != null) {
                        vm.setVehicle(vehicle)
                    }
                })

        // Distance
        b.expandTrip.setOnClickListener {
            expandAddresses(b)
            b.startingAddress.requestFocus()
        }
        if (!TextUtils.isEmpty(vm.allowance.from) || !TextUtils.isEmpty(vm.allowance.to)) {
            expandAddresses(b)
        }

        // Addresses
        val googleApiClient = GoogleApiClientData(this) { addApi(Places.GEO_DATA_API) }
        val startingAddressAdapter = LocationHintsAdapter(googleApiClient, this)
        b.startingAddress.setAdapter(startingAddressAdapter)
        b.startingAddress.onItemClickListener = startingAddressAdapter.getItemListener(vm.startingCurrentPlaceTrigger)
        vm.startingCurrentPlace.observe(this, Observer { places ->
            startingAddressAdapter.setCurrentPlaces(places)
            b.startingAddress.showDropDown()
        })
        b.startingAddress.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (TextUtils.isEmpty(b.arrivalAddress.text)) {
                    b.arrivalAddress.requestFocus()
                } else {
                    vm.directionsTrigger.trigger()
                    Views.hideSoftInput(textView)
                }
                true
            } else false
        }
        val arrivalAddressAdapter = LocationHintsAdapter(googleApiClient, this)
        b.arrivalAddress.setAdapter(arrivalAddressAdapter)
        b.arrivalAddress.onItemClickListener = arrivalAddressAdapter.getItemListener(vm.arrivalCurrentPlaceTrigger)
        vm.arrivalCurrentPlace.observe(this, Observer { places ->
            arrivalAddressAdapter.setCurrentPlaces(places)
            b.arrivalAddress.showDropDown()
        })
        b.arrivalAddress.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                vm.directionsTrigger.trigger()
                Views.hideSoftInput(textView)
                true
            } else false
        }
        b.arrivalAddress.setOnItemClickListener { _, _, _, _ ->
            vm.directionsTrigger.trigger()
            Views.hideSoftInput(b.arrivalAddress)
        }
        vm.locationPermissionCheck.handleOn(this, "location_permission")
        vm.officeAddress.observe(this, Observer { address ->
            if (address != null) {
                startingAddressAdapter.setOfficeAddress(address)
                arrivalAddressAdapter.setOfficeAddress(address)
            }
        })

        // Map
        b.map.onCreate(savedInstanceState)
        b.map.getMapAsync { map ->
            map.setOnMapClickListener { vm.directionsTrigger.trigger() }
            map.uiSettings.isMapToolbarEnabled = false
        }
        vm.directions.observe(this, Observer { result ->
            vm.directionsLoading.postValue(false)
            if (result != null) {
                if (result.routes.isEmpty()) {
                    Snackbar.make(b.root, R.string.no_directions_found, Snackbar.LENGTH_SHORT).show()
                } else {
                    var distance = GeoUtils.getDistance(result.routes[0])
                    if (b.roundTrip.isChecked) distance *= 2
                    val currentDistance = vm.allowance.distance
                    if (currentDistance != null && currentDistance.toDouble() != distance) {
                        Snackbar.make(b.root, R.string.distance_updated, Snackbar.LENGTH_LONG)
                                .setAction(android.R.string.cancel) { vm.setDistance(currentDistance) }
                                .show()
                    }
                    vm.setPolyline(result.routes[0].overviewPolyline, distance.toInt())
                }
            }
        })
        vm.directionsLoading.observe(this, Observer { loading ->
            if (loading == true)
                b.mapProgress.show()
            else
                b.mapProgress.hide()
        })

        // Round trip
        b.roundTrip.setOnCheckedChangeListener { _, checked -> vm.toggleRoundTrip(checked) }

        // Dates
        b.datesLayout.setOnClickListener {
            DatesPickerFragment.newInstance(vm.allowance.dates)
                    .showForResult(this, "dates_picker")
        }
        ArchDialogs.resultOf(DatesPickerFragment::class.java, "dates_picker", this)
                .observe(this, Observer { if (it != null) vm.setDates(it) })

        // Data
        vm.allowanceData.observe(this, Observer { exp ->
            b.allowance = exp

            // Bind dates
            val dateFormat = fullDateFormat()
            b.dates.text = TextUtils.join("\n", exp?.dates?.map { dateFormat.format(it) })

        })
        vm.vehicle.observe(this, Observer { b.vehicle = it })
        vm.clients.observe(this, Observer { clients ->
            hintsAdapter.setClients(clients)
            startingAddressAdapter.setClients(clients)
            arrivalAddressAdapter.setClients(clients)
        })
    }

    private fun expandAddresses(b: AllowanceActivityBinding) {
        b.locationStartIcon.visibility = View.VISIBLE
        b.startingAddress.visibility = View.VISIBLE
        b.arrivalAddress.visibility = View.VISIBLE
        b.map.visibility = View.VISIBLE
        b.roundTrip.visibility = View.VISIBLE
        b.expandTrip.visibility = View.GONE
        if (java.lang.Boolean.TRUE == vm.directionsLoading.value) {
            b.mapProgress.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.allowance, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class VM(application: Application) : AndroidViewModel(application) {

        val allowanceData = MutableLiveData<MileageAllowance>()
        val allowance: MileageAllowance get() = allowanceData.value!!

        val vehicle = allowanceData.switchMap {
            if (it.vehicleId != 0L) {
                DataRepository.of(application).vehicle(it.vehicleId)
            } else {
                SimpleData(Optional.absent())
            }
        }

        val clients = DataRepository.of(getApplication()).clients()
        val officeAddress = DataRepository.of(getApplication()).officeAddress()

        val locationPermissionCheck = PermissionCheck(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
        private val currentPlace: LiveData<PlaceLikelihoodBuffer> = locationPermissionCheck.allGranted().switchMap { granted ->
            @SuppressLint("MissingPermission")
            if (granted)
                GoogleApiClientData(getApplication()) { addApi(Places.PLACE_DETECTION_API) }.cancellingSwitchMap { c ->
                    Places.PlaceDetectionApi.getCurrentPlace(c, null)
                            .toData()
                }
            else
                null
        }
        val startingCurrentPlaceTrigger = EventData<Any>()
        val arrivalCurrentPlaceTrigger = EventData<Any>()
        val startingCurrentPlace = startingCurrentPlaceTrigger.switchMap { currentPlace }
        val arrivalCurrentPlace = arrivalCurrentPlaceTrigger.switchMap { currentPlace }
        val directionsTrigger = EventData<Any>()
        val directionsLoading = MutableLiveData<Boolean>()
        val directions = directionsTrigger.cancellingSwitchMap {
            val exp = allowanceData.value
            if (exp != null && !TextUtils.isEmpty(exp.from) && !TextUtils.isEmpty(exp.to)) {
                directionsLoading.postValue(true)
                DirectionsApi.getDirections(GeoUtils.getGeoApiContext(application), exp.from, exp.to)
                        .mode(TravelMode.DRIVING)
                        .alternatives(false)
                        .toData()
            } else {
                null
            }
        }

        init {
            val exp = MileageAllowance()
            exp.dates = TreeSet(setOf(Date()))
            allowanceData.value = exp
            val defaultVehicleId = DataRepository.of(getApplication()).defaultVehicleId()
            defaultVehicleId.observeForever(object : Observer<Optional<Long>> {
                override fun onChanged(t: Optional<Long>?) {
                    exp.vehicleId = t!!.or(0L)
                    defaultVehicleId.removeObserver(this)
                }
            })
        }

        fun setVehicle(vehicle: Vehicle) {
            allowanceData.update { vehicleId = vehicle.id }
        }

        fun setClient(client: Client) {
            allowanceData.update {
                reason = client.name
                to = client.address
            }
        }

        fun setDates(dates: Collection<Date>) {
            allowanceData.update {
                this.dates = HashSet(dates)
            }
        }

        fun setPolyline(polyline: EncodedPolyline, distance: Int) {
            allowanceData.update {
                this.polyline = polyline
                this.distance = distance
            }
        }

        fun setDistance(distance: Int) {
            allowanceData.update {
                this.distance = distance
            }
        }

        fun toggleRoundTrip(roundTrip: Boolean) {
            allowanceData.update {
                val d = distance
                if (d != null) {
                    distance = if (roundTrip) d * 2 else d / 2
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            val b = currentPlace.value
            b?.release()
        }

    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, AllowanceActivity::class.java)
        }
    }

}
