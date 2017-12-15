package com.cubber.tiime.app.mileages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.OnRebindCallback
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
import com.cubber.tiime.model.Associate
import com.cubber.tiime.model.Client
import com.cubber.tiime.model.MileageAllowance
import com.cubber.tiime.model.Vehicle
import com.cubber.tiime.utils.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.model.TravelMode
import com.wapplix.arch.*
import com.wapplix.binding.setContentViewBinding
import com.wapplix.maps.GeoUtils
import com.wapplix.showSnackbar
import java.util.*
import kotlin.collections.HashSet

/**
 * An activity to create and save a new [MileageAllowance] item.
 */
class AllowanceActivity : AppCompatActivity() {

    private lateinit var vm: VM
    private lateinit var binding: AllowanceActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = getUiModel()

        binding = setContentViewBinding(R.layout.allowance_activity)
        binding.addOnRebindCallback(object : OnRebindCallback<AllowanceActivityBinding>() {

            private var mapHelper = PolylineMapHelper(binding.map)
            private var mapOptionsFactory = PolylineMapOptionsFactory(this@AllowanceActivity)

            override fun onBound(binding: AllowanceActivityBinding) {
                val polyline = binding.allowance?.polyline
                mapHelper.applyOptions(if (polyline != null) mapOptionsFactory.create(polyline) else null)
            }

        })

        // Action bar
        setSupportActionBar(binding.toolbar)
        with(supportActionBar!!) {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_close_24dp)
        }

        // Purpose/client hints
        val hintsAdapter = AllowanceHintAdapter()
        binding.hints.adapter = hintsAdapter
        binding.purpose.addTextChangedListener(afterTextChanged = {
            hintsAdapter.filter.filter(binding.purpose.text)
            if (binding.purpose.hasFocus()) vm.purposeHintsShown.value = true
        })
        binding.purpose.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) vm.purposeHintsShown.value = false }
        vm.purposeHintsShown.observe(this, Observer { editing ->
            binding.hints.visibility = if (editing == true) View.VISIBLE else View.GONE
        })
        binding.purpose.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL -> {
                    vm.purposeHintsShown.value = false
                    if (TextUtils.isEmpty(binding.distance.text)) {
                        binding.distance.requestFocus()
                    }
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            binding.purpose.requestFocus()
            vm.purposeHintsShown.value = true
        }
        hintsAdapter.onClientHintClick = { client ->
            vm.setClient(client)
            expandAddresses(binding)
            vm.purposeHintsShown.value = false
            binding.fromAddress.requestFocus()
        }

        // Vehicle
        binding.vehicleLayout.setOnClickListener { vm.showVehiclePicker() }
        binding.addVehicleCard.setOnClickListener { vm.showVehicleCardPicker() }

        // Distance
        binding.expandTrip.setOnClickListener {
            expandAddresses(binding)
            binding.fromAddress.requestFocus()
        }
        if (!TextUtils.isEmpty(vm.allowance.fromAddress) || !TextUtils.isEmpty(vm.allowance.toAddress)) {
            expandAddresses(binding)
        }

        // Addresses
        val resolveNextAddressFocus = {
            when {
                binding.fromAddress.text.isNullOrEmpty() -> {
                    binding.fromAddress.requestFocus()
                    binding.fromAddress.showDropDown()
                }
                binding.toAddress.text.isNullOrEmpty() -> {
                    binding.toAddress.requestFocus()
                    binding.toAddress.showDropDown()
                }
                else -> {
                    vm.computeDirections()
                    binding.fromAddress.dismissDropDown()
                    binding.toAddress.dismissDropDown()
                    binding.toAddress.hideSoftInput()
                }
            }
        }
        val autocompleteClient = Places.getGeoDataClient(this, null)
        val fromAddressAdapter = LocationHintsAdapter(autocompleteClient)
        binding.fromAddress.setAdapter(fromAddressAdapter)
        binding.fromAddress.setOnItemClickListener { _, _, i, _ ->
            if (fromAddressAdapter.isCurrentLocationItemPosition(i)) vm.addCurrentLocationHints()
            else resolveNextAddressFocus()
        }
        binding.fromAddress.setOnEditorActionListener(
                onNext = { resolveNextAddressFocus(); true }
        )
        val toAddressAdapter = LocationHintsAdapter(autocompleteClient)
        binding.toAddress.setAdapter(toAddressAdapter)
        binding.toAddress.setOnEditorActionListener(
                onDone = { resolveNextAddressFocus(); true }
        )
        binding.toAddress.setOnItemClickListener { _, _, i, _ ->
            if (toAddressAdapter.isCurrentLocationItemPosition(i)) vm.addCurrentLocationHints()
            else resolveNextAddressFocus()
        }
        vm.associate.observe(this, Observer { associate ->
            associate?.defaultFromAddress?.let { address ->
                fromAddressAdapter.setOfficeAddress(address)
                toAddressAdapter.setOfficeAddress(address)
            }
        })
        vm.currentPlaces.observe(this, Observer { places ->
            fromAddressAdapter.setCurrentPlaces(places)
            toAddressAdapter.setCurrentPlaces(places)
            if (binding.fromAddress.hasFocus()) binding.fromAddress.showDropDown()
            else if (binding.toAddress.hasFocus()) binding.toAddress.showDropDown()
        })
        vm.currentPlacesLoading.observe(this, Observer { loading ->
            fromAddressAdapter.currentPlacesLoading = loading ?: false
            toAddressAdapter.currentPlacesLoading = loading ?: false
        })

        // Map
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync { map ->
            map.setOnMapClickListener { vm.computeDirections() }
            map.uiSettings.isMapToolbarEnabled = false
        }
        vm.directionsLoading.observe(this, Observer { loading ->
            if (loading == true) binding.mapProgress.show()
            else binding.mapProgress.hide()
        })

        // Round trip
        binding.roundTrip.setOnCheckedChangeListener { _, checked -> vm.toggleRoundTrip(checked) }

        // Dates
        binding.datesLayout.setOnClickListener { vm.showDatesPicker() }

        // Data
        vm.allowanceData.observe(this, Observer { exp ->
            binding.allowance = exp

            // Bind dates
            val dateFormat = fullDateFormat()
            binding.dates.text = TextUtils.join("\n", exp?.dates?.map { dateFormat.format(it) })

        })
        vm.vehicle.observe(this, Observer { binding.vehicle = it })
        vm.vehicles.observe(this, Observer { binding.noVehicle = it?.isEmpty() ?: false })
        vm.clients.observe(this, Observer { clients ->
            hintsAdapter.setClients(clients)
            fromAddressAdapter.setClients(clients)
            toAddressAdapter.setClients(clients)
        })
        vm.cardProcessing.observe(this, Observer { binding.cardProcessing = it ?: false })
    }

    private fun expandAddresses(b: AllowanceActivityBinding) {
        b.locationStartIcon.visibility = View.VISIBLE
        b.fromAddress.visibility = View.VISIBLE
        b.toAddress.visibility = View.VISIBLE
        b.mapFrame.visibility = View.VISIBLE
        b.roundTrip.visibility = View.VISIBLE
        b.expandTrip.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.allowance, menu)
        val editDoneItem = menu.findItem(R.id.edit_done)
        val saveItem = menu.findItem(R.id.save)
        vm.purposeHintsShown.observe(this, Observer { shown ->
            editDoneItem.isVisible = shown == true
            saveItem.isVisible = shown != true

        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_done -> {
                vm.purposeHintsShown.value = false; true
            }
            R.id.save -> {
                validate(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validate() {
        val allowance = vm.allowance

        if (allowance.purpose.isNullOrBlank()) {
            showSnackbar(R.string.allowance_purpose_mandatory_error)
            binding.purpose.requestFocus()
            return
        }

        vm.save()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class VM(application: Application) : UiModel<AllowanceActivity>(application) {

        val purposeHintsShown = MutableLiveData<Boolean>()

        private val repository = DataRepository.of(application)

        val allowanceData = MutableLiveData<MileageAllowance>()
        val allowance get() = checkNotNull(allowanceData.value)

        val associate = repository.associate()

        val vehicles = repository.vehicles().toLiveData()
        val vehicle = MediatorLiveData<Vehicle>().apply {
            val select = {
                vehicles.value?.let {
                    value = it.firstOrNull { it.id == allowance.vehicleId }
                }
            }
            addSource(allowanceData) { select() }
            addSource(vehicles) { select() }
        }

        val clients = repository.clients()

        var cardProcessing = ProgressData<Vehicle>()

        init {
            val exp = MileageAllowance()
            exp.dates = TreeSet(setOf(Date()))
            allowanceData.value = exp
            associate.observeForever(object : Observer<Associate> {
                override fun onChanged(a: Associate?) {
                    exp.vehicleId = a?.defaultVehicleId ?: 0L
                    associate.removeObserver(this)
                }
            })
        }

        val currentPlaces = MutableLiveData<List<LocationHintsAdapter.Item>>()
        val currentPlacesLoading = MutableLiveData<Boolean>()

        @SuppressLint("MissingPermission")
        fun addCurrentLocationHints() {
            requirePermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), "location_permission") {
                currentPlacesLoading.postValue(true)
                Places.getPlaceDetectionClient(getApplication<Application>(), null)
                        .getCurrentPlace(null)
                        .addOnSuccessListener { r ->
                            currentPlaces.postValue(r.map { p -> LocationHintsAdapter.Item(p.place) })
                            r.release()
                        }
                        .addOnFailureListener { e -> onUi { showErrorSnackbar(e) } }
                        .addOnCompleteListener { currentPlacesLoading.postValue(false) }
            }
        }

        val directionsLoading = MutableLiveData<Boolean>()
        private var directionsRequest: DirectionsApiRequest? = null

        fun computeDirections() {
            directionsLoading.postValue(true)
            directionsRequest?.cancel()
            directionsRequest = DirectionsApi.getDirections(GeoUtils.getGeoApiContext(getApplication()), allowance.fromAddress, allowance.toAddress)
                    .mode(TravelMode.DRIVING)
                    .alternatives(false)
            directionsRequest?.setCallback(
                    onResult = { r ->
                        if (r != null) {
                            if (r.routes.isEmpty()) {
                                onUi { showSnackbar(R.string.no_directions_found, Snackbar.LENGTH_SHORT) }
                                allowanceData.update { polyline = null }
                            } else {
                                var newDistance = GeoUtils.getDistance(r.routes[0])
                                if (allowance.roundTrip == true) newDistance *= 2
                                val currentDistance = allowance.distance
                                if (currentDistance != null && currentDistance.toDouble() != newDistance) {
                                    onUi {
                                        Snackbar.make(binding.root, R.string.distance_updated, Snackbar.LENGTH_LONG)
                                                .setAction(android.R.string.cancel) {
                                                    allowanceData.update { distance = currentDistance }
                                                }
                                                .show()
                                    }
                                }
                                allowanceData.update {
                                    distance = newDistance.toInt()
                                    polyline = r.routes[0].overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }
                                }
                            }
                        }
                        directionsLoading.postValue(false)
                    },
                    onFailure = { e ->
                        onUi { showErrorSnackbar(e) }
                        directionsLoading.postValue(false)
                    }
            )
        }

        fun showVehiclePicker() {
            onUi {
                VehiclePickerFragment().show(supportFragmentManager, "vehicle_picker") {
                    allowanceData.update { vehicleId = it }
                }
            }
        }

        internal fun showVehicleCardPicker() {
            onUi {
                startActivityForResult(
                        Intents.getContent(Uris.SUPPORTED_TYPES, getString(R.string.add_vehicle_card)),
                        "add_card"
                ) { code, data ->
                    if (code == Activity.RESULT_OK && data != null) {
                        if (Uris.checkSupportedType(this@VM.getApplication(), data.data) == true) {
                            vehicle.value?.let { v ->
                                repository.saveVehicle(v.copy(card = data.data))
                                        .compose(cardProcessing)
                                        .subscribe(
                                                { onUi { showSnackbar(R.string.vehicle_card_saved) } },
                                                { e -> onUi { showErrorSnackbar(e) } }
                                        )
                            }
                        } else {
                            onUi { showSnackbar(R.string.unsupported_type_error) }
                        }
                    }
                }
            }
        }

        fun setClient(client: Client) {
            allowanceData.update {
                purpose = client.name
                toAddress = client.directionsAddress
            }
        }

        fun showDatesPicker() {
            onUi {
                DatesPickerFragment.newInstance(allowance.dates).show(supportFragmentManager, "dates_picker") {
                    allowanceData.update { dates = HashSet(it) }
                }
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

        internal fun save() =
                DataRepository.of(getApplication()).saveAllowance(allowance)
                        .subscribe(
                                { onUi { finish() } },
                                { e -> onUi { showErrorSnackbar(e) } }
                        )


    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, AllowanceActivity::class.java)
        }
    }

}
