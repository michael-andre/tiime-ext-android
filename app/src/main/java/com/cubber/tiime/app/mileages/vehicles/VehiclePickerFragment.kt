package com.cubber.tiime.app.mileages.vehicles

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.data.DataRepository
import com.cubber.tiime.databinding.VehiclePickerBinding
import com.cubber.tiime.databinding.VehiclePickerItemBinding
import com.cubber.tiime.model.Vehicle
import com.cubber.tiime.utils.showErrorSnackbar
import com.wapplix.arch.*
import com.wapplix.recycler.BindingListAdapter
import com.wapplix.showSnackbar
import com.wapplix.widget.setOverflowPopupMenu

/**
 * Created by mike on 26/09/17.
 */

class VehiclePickerFragment : AppCompatDialogFragment(), ResultEmitter<Long>, DialogInterface.OnShowListener {

    private lateinit var vm: VM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = VehiclePickerBinding.inflate(LayoutInflater.from(context))
        binding.list.adapter = VehiclesAdapter()

        vm = getUiModel()
        vm.vehicles.observe(this, Observer { binding.vehicles = it })

        val dialog = AlertDialog.Builder(context!!)
                .setTitle(R.string.vehicle)
                .setView(binding.root)
                .setNeutralButton(R.string.add_vehicle, null)
                .create()
        dialog.setOnShowListener(this)
        return dialog
    }

    override fun onShow(dialog: DialogInterface) {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            VehicleEditorFragment().show(fragmentManager!!, "create_vehicle")
        }
    }

    private inner class VehiclesAdapter : BindingListAdapter<Vehicle, VehiclePickerItemBinding>() {

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
            binding.root.setOnClickListener {
                result.onResult(item.id)
                dismiss()
            }
        }

    }

    class VM(application: Application) : UiModel<VehiclePickerFragment>(application) {

        internal var vehicles = DataRepository.of(getApplication()).vehicles().toLiveData()

        internal fun deleteVehicle(id: Long) {
            onUi {
                showConfirm(
                        titleRes = R.string.delete_vehicle_prompt,
                        messageRes = R.string.delete_vehicle_message,
                        positiveButtonRes = R.string.delete,
                        negativeButtonRes = android.R.string.cancel,
                        tag = "delete_vehicle"
                ) {
                    DataRepository.of(getApplication()).deleteVehicle(id)
                            .subscribe(
                                    { onUi { showSnackbar(R.string.vehicle_deleted) } },
                                    { e -> onUi { showErrorSnackbar(e) } }
                            )
                }
            }
        }

    }

}