package com.cubber.tiime.app.mileages.vehicles

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.cubber.tiime.BR
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.VehicleEditorBinding
import com.cubber.tiime.model.Vehicle
import com.cubber.tiime.utils.Intents
import com.cubber.tiime.utils.Uris
import com.cubber.tiime.utils.vehicleFiscalPowerName
import com.wapplix.ProgressDialogFragment
import com.wapplix.arch.SingleLiveEvent
import com.wapplix.arch.update
import com.wapplix.widget.SimpleAdapter
import com.wapplix.widget.SimpleBindingListAdapter
import com.wapplix.withArguments

/**
 * Created by mike on 26/09/17.
 */

class VehicleEditorFragment : AppCompatDialogFragment() {

    private lateinit var vm: VM
    private lateinit var binding: VehicleEditorBinding

    private val vehicleId: Long?
        get() = arguments?.getLong(ARG_VEHICLE_ID)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = VehicleEditorBinding.inflate(LayoutInflater.from(context))

        val typesAdapter = SimpleBindingListAdapter<String>(
                context!!,
                R.layout.vehicle_type_spinner_item,
                R.layout.vehicle_type_spinner_dropdown_item,
                BR.type
        )
        binding.type.adapter = typesAdapter
        binding.types = Vehicles.getVehicleTypes()

        val powersAdapter = object : SimpleAdapter<String>(context!!, android.R.layout.simple_spinner_item, R.layout.support_simple_spinner_dropdown_item) {
            override fun onBindView(view: View, @Vehicle.FiscalPower item: String) {
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.text = vehicleFiscalPowerName(context!!, item)
            }
        }
        binding.fiscalPower.adapter = powersAdapter
        binding.setTypeListener { _, _, _, _ -> binding.powers = Vehicles.getAvailablePowers(binding.type.selectedItem as String) }

        binding.addVehicleCard.setOnClickListener { showVehicleCardPicker() }
        binding.vehicleCard.setOnClickListener {
            val uri = vm.vehicle.value?.card
            if (uri != null) Intents.startViewActivity(context!!, uri)
        }
        binding.vehicleCardMenu.setOnClickListener { v ->
            val menu = PopupMenu(context!!, v, Gravity.END)
            menu.inflate(R.menu.attachment_context)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.replace -> {
                        showVehicleCardPicker()
                        true
                    }
                    R.id.delete -> {
                        vm.vehicle.update { card = null }
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        vm = ViewModelProviders.of(this).get(VM::class.java)
        val vehicle = savedInstanceState?.getParcelable<Vehicle>(STATE_VEHICLE)
        vm.init(vehicleId, vehicle)

        vm.savingState.observe(this, Observer { saving ->
            if (saving == true) ProgressDialogFragment().show(childFragmentManager, "progress")
            else (childFragmentManager.findFragmentByTag("progress") as DialogFragment?)?.dismiss()
        })
        vm.savedEvent.observe(this, Observer { dismiss() })
        vm.errorEvent.observe(this, Observer { Snackbar.make(binding.root, R.string.generic_error_message, Snackbar.LENGTH_SHORT).show() })

        val dialog = AlertDialog.Builder(context!!)
                .setTitle(if (vehicleId == null) R.string.new_vehicle else R.string.edit_vehicle)
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        dialog.setOnShowListener {
            val okBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            okBtn.isEnabled = false
            okBtn.setOnClickListener { validate() }
            vm.vehicle.observe(this, Observer {
                binding.vehicle = it
                okBtn.isEnabled = it != null
            })
        }
        return dialog
    }

    private fun validate() {
        val vehicle = vm.vehicle.value!!

        if (vehicle.name.isNullOrBlank()) {
            binding.nameLayout.error = getString(R.string.required)
            binding.name.requestFocus()
            return
        }
        binding.nameLayout.error = null

        vm.save()
    }

    private fun showVehicleCardPicker() {
        startActivityForResult(Intents.getContent(Uris.SUPPORTED_TYPES, getString(R.string.add_vehicle_card)), REQUEST_ADD_VEHICLE_CARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_VEHICLE_CARD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val vehicle = vm.vehicle.value
                    vehicle!!.card = data.data
                    vm.vehicle.value = vehicle
                }
                return
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_VEHICLE, vm.vehicle.value)
    }

    class VM(application: Application) : AndroidViewModel(application) {

        lateinit var vehicle: MutableLiveData<Vehicle>
        var savingState = MutableLiveData<Boolean>()
        var savedEvent = SingleLiveEvent<Any>()
        var errorEvent = SingleLiveEvent<Throwable>()

        internal fun init(id: Long?, savedVehicle: Vehicle?) {
            if (!this::vehicle.isInitialized) {
                vehicle = MutableLiveData()
                when {
                    savedVehicle != null -> vehicle.value = savedVehicle
                    id == null -> vehicle.value = Vehicle()
                    else -> DataRepository.of(getApplication()).vehicle(id)
                            .firstElement()
                            .subscribe { vehicle.postValue(it.copy()) }
                }
            }
        }

        internal fun save() =
                DataRepository.of(getApplication()).saveVehicle(vehicle.value!!)
                        .doOnEvent { _, _ -> savingState.postValue(false) }
                        .subscribe(
                                { _ -> savedEvent.trigger() },
                                { e -> errorEvent.trigger(e) }
                        )

    }

    companion object {

        private const val ARG_VEHICLE_ID = "vehicle_id"
        private const val STATE_VEHICLE = "vehicle"
        private const val REQUEST_ADD_VEHICLE_CARD = 1

        fun newInstance(vehicleId: Long) = VehicleEditorFragment().withArguments {
            putLong(ARG_VEHICLE_ID, vehicleId)
        }

    }

}
