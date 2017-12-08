package com.cubber.tiime.app.mileages.vehicles

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.VehiclePickerBinding
import com.cubber.tiime.databinding.VehiclePickerItemBinding
import com.cubber.tiime.model.Vehicle
import com.wapplix.ResultDialogFragment
import com.wapplix.arch.SingleLiveEvent
import com.wapplix.arch.UiModel
import com.wapplix.arch.toLiveData
import com.wapplix.recycler.BindingListAdapter
import com.wapplix.widget.setOverflowPopupMenu

/**
 * Created by mike on 26/09/17.
 */

class VehiclePickerFragment : ResultDialogFragment<Vehicle>(), DialogInterface.OnShowListener {

    private lateinit var vm: VM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val b = VehiclePickerBinding.inflate(LayoutInflater.from(context))
        val adapter = Adapter()
        b.list.adapter = adapter

        vm = ViewModelProviders.of(this).get(VM::class.java)
        vm.vehicles.observe(this, Observer { b.vehicles = it })

        vm.handleOn(this)
        vm.errorEvent.observe(this, Observer { e ->
            Snackbar.make(b.root, R.string.generic_error_message, Snackbar.LENGTH_LONG).show()
        })

        val dialog = AlertDialog.Builder(context!!)
                .setTitle(R.string.vehicle)
                .setView(b.root)
                .setNeutralButton(R.string.add_vehicle, null)
                .create()
        dialog.setOnShowListener(this)
        return dialog
    }

    override fun onShow(dialog: DialogInterface) {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { VehicleEditorFragment().show(fragmentManager!!, "create_vehicle") }
    }

    private inner class Adapter : BindingListAdapter<Vehicle, VehiclePickerItemBinding>() {

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VehiclePickerItemBinding {
            val binding = VehiclePickerItemBinding.inflate(inflater, parent, false)
            binding.menu.setOverflowPopupMenu(R.menu.vehicle_context) {
                when (it) {
                    R.id.edit -> VehicleEditorFragment.newInstance(binding.vehicle!!.id).show(fragmentManager, "edit_vehicle")
                    R.id.delete -> vm.deleteVehicle(binding.vehicle!!.id)
                }
            }
            return binding

        }

        override fun onBindView(binding: VehiclePickerItemBinding, item: Vehicle) {
            binding.vehicle = item
            binding.root.setOnClickListener { sendResult(item) }
        }

    }

    class VM(application: Application) : UiModel(application) {

        internal val errorEvent = SingleLiveEvent<Throwable>()

        internal var vehicles = DataRepository.of(getApplication()).vehicles().toLiveData()

        internal fun deleteVehicle(id: Long) {
            showConfirm(title = R.string.delete_vehicle_prompt, message = R.string.delete_vehicle_message, positiveButton = R.string.delete, tag = "delete_vehicle")
                    .flatMapCompletable { DataRepository.of(getApplication()).deleteVehicle(id) }
                    .subscribe({}, { e -> errorEvent.trigger(e) })
        }

    }

}
