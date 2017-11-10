package com.cubber.tiime.app.mileages.vehicles

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
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
import com.cubber.tiime.utils.vehicleFiscalPowerName
import com.wapplix.arch.update
import com.wapplix.widget.SimpleAdapter
import com.wapplix.widget.SimpleBindingListAdapter

/**
 * Created by mike on 26/09/17.
 */

class VehicleEditorFragment : AppCompatDialogFragment() {

    private lateinit var vm: VM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = VehicleEditorBinding.inflate(LayoutInflater.from(context))

        val typesAdapter = SimpleBindingListAdapter<String>(
                context!!,
                R.layout.vehicle_type_spinner_item,
                R.layout.vehicle_type_spinner_dropdown_item,
                BR.type
        )
        b.type.adapter = typesAdapter

        val powersAdapter = object : SimpleAdapter<String>(context!!, android.R.layout.simple_spinner_item, R.layout.support_simple_spinner_dropdown_item) {
            override fun onBindView(view: View, @Vehicle.FiscalPower item: String) {
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.text = vehicleFiscalPowerName(context!!, item)
            }
        }
        b.fiscalPower.adapter = powersAdapter
        b.setTypeListener { _, _, _, _ -> b.powers = Vehicles.getAvailablePowers(b.type.selectedItem as String) }

        b.addVehicleCard.setOnClickListener { showVehicleCardPicker() }
        b.vehicleCard.setOnClickListener {
            val uri = vm.vehicle.value?.card_uri
            if (uri != null) Intents.startViewActivity(context!!, uri) }
        b.vehicleCardMenu.setOnClickListener { v ->
            val menu = PopupMenu(context!!, v, Gravity.END)
            menu.inflate(R.menu.attachment_context)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.replace -> {
                        showVehicleCardPicker()
                        true
                    }
                    R.id.delete -> {
                        vm.vehicle.update { card_uri = null }
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        vm = ViewModelProviders.of(this).get(VM::class.java)
        if (vm.vehicle.value == null) {
            vm.vehicle.value = Vehicle()
        }

        vm.vehicleTypes.observe(this, Observer { b.types = it })
        vm.vehicle.observe(this, Observer { b.vehicle = it })

        return AlertDialog.Builder(context!!)
                .setTitle(R.string.new_vehicle)
                .setView(b.root)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    private fun showVehicleCardPicker() {
        startActivityForResult(Intents.getContent(Intents.DOCUMENT_TYPES, getString(R.string.add_vehicle_card)), REQUEST_ADD_VEHICLE_CARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_VEHICLE_CARD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val vehicle = vm.vehicle.value
                    vehicle!!.card_uri = data.data
                    vm.vehicle.value = vehicle
                }
                return
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    class VM(application: Application) : AndroidViewModel(application) {

        internal var vehicle = MutableLiveData<Vehicle>()
        internal var vehicleTypes = DataRepository.of(getApplication()).vehicleTypes()

    }

    companion object {
        private const val REQUEST_ADD_VEHICLE_CARD = 1
    }

}
