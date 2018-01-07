package com.cubber.tiime.app.mileages.vehicles

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import com.cubber.tiime.BR
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.VehicleEditorBinding
import com.cubber.tiime.model.Vehicle
import com.cubber.tiime.utils.Intents
import com.cubber.tiime.utils.Uris
import com.cubber.tiime.utils.showErrorSnackbar
import com.cubber.tiime.utils.vehicleFiscalPowerName
import com.wapplix.ProgressDialogFragment
import com.wapplix.arch.UiModel
import com.wapplix.arch.getUiModel
import com.wapplix.arch.startActivityForResult
import com.wapplix.arch.update
import com.wapplix.showSnackbar
import com.wapplix.widget.SimpleAdapter
import com.wapplix.widget.SimpleBindingListAdapter
import com.wapplix.widget.setPopupMenu
import com.wapplix.withArguments
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by mike on 26/09/17.
 */

class VehicleEditorFragment : AppCompatDialogFragment() {

    private lateinit var vm: VM
    private lateinit var binding: VehicleEditorBinding

    private val vehicleId: Long?
        get() = arguments?.getLong(ARG_VEHICLE_ID)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = VehicleEditorBinding.inflate(LayoutInflater.from(context))!!

        binding.type.adapter = SimpleBindingListAdapter<String>(
                context!!,
                R.layout.vehicle_type_spinner_item,
                R.layout.vehicle_type_spinner_dropdown_item,
                BR.type
        )
        binding.types = Vehicles.getVehicleTypes()

        val powersAdapter = SimpleAdapter<String>(context!!, { vehicleFiscalPowerName(context!!, it)!! })
        binding.fiscalPower.adapter = powersAdapter
        binding.setTypeListener { _, _, _, _ -> binding.powers = Vehicles.getAvailablePowers(binding.type.selectedItem as String) }

        binding.addVehicleCard.setOnClickListener { vm.showCardPicker() }
        binding.vehicleCard.setOnClickListener {
            val uri = vm.vehicle.value?.card
            if (uri != null) Intents.startViewActivity(context!!, uri)
        }
        binding.vehicleCardMenu.setPopupMenu(R.menu.attachment_context) {
            when (it) {
                R.id.replace -> vm.showCardPicker()
                R.id.delete -> vm.vehicle.update { card = null }
            }
        }

        vm = getUiModel()
        val vehicle = savedInstanceState?.getParcelable<Vehicle>(STATE_VEHICLE)
        vm.init(vehicleId, vehicle)

        vm.savingState.observe(this, Observer { saving ->
            if (saving == true) ProgressDialogFragment().show(childFragmentManager, "progress")
            else (childFragmentManager.findFragmentByTag("progress") as DialogFragment?)?.dismiss()
        })

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_VEHICLE, vm.vehicle.value)
    }

    class VM(application: Application) : UiModel<VehicleEditorFragment>(application) {

        lateinit var vehicle: MutableLiveData<Vehicle>
        var savingState = MutableLiveData<Boolean>()

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

        internal fun showCardPicker() {
            onUi {
                startActivityForResult(
                        Intents.getContent(Uris.SUPPORTED_TYPES, getString(R.string.add_vehicle_card)),
                        "add_card"
                ) { code, data -> if (code == Activity.RESULT_OK && data != null) {
                    if (Uris.checkSupportedType(getApplication(), data.data) == true) {
                        vehicle.update { card = data.data }
                    } else {
                        onUi { showSnackbar(R.string.unsupported_type_error) }
                    }
                } }
            }
        }

        internal fun save() =
                DataRepository.of(getApplication()).saveVehicle(vehicle.value!!)
                        .doOnEvent { _, _ -> savingState.postValue(false) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { onUi { dismiss() } },
                                { e -> onUi { showErrorSnackbar(e) } }
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
