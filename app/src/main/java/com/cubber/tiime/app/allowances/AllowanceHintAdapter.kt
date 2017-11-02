package com.cubber.tiime.app.allowances

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import com.cubber.tiime.R
import com.cubber.tiime.databinding.AllowanceHintClientItemBinding
import com.cubber.tiime.model.Client
import com.cubber.tiime.utils.filterCleaned
import com.wapplix.recycler.BindingListAdapter
import java.util.*

/**
 * Created by mike on 28/09/17.
 */

class AllowanceHintAdapter : BindingListAdapter<Any, ViewDataBinding>(), Filterable {

    private val filter = HintsFilter()
    private var clients: List<Client>? = null

    var onClientHintClick: ((Client) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item is Client) {
            R.layout.allowance_hint_client_item
        } else super.getItemViewType(position)
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(inflater, viewType, parent, false)
    }

    override fun onBindView(binding: ViewDataBinding, item: Any) {
        if (binding is AllowanceHintClientItemBinding) {
            val client = item as Client
            binding.client = client
            binding.getRoot().setOnClickListener { _ -> onClientHintClick?.invoke(client) }
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    fun getQueryWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                val query = editText.text.toString()
                filter.filter(query)
            }

        }
    }

    fun setClients(clients: List<Client>?) {
        this.clients = clients
        filter.notifySourceDataChanged()
    }

    private inner class HintsFilter : Filter() {

        private var mLastQuery: CharSequence? = null

        fun notifySourceDataChanged() {
            filter(mLastQuery)
        }

        override fun performFiltering(query: CharSequence?): Filter.FilterResults {
            mLastQuery = query
            val results = Filter.FilterResults()
            val values = ArrayList<Any>()
            val clients = this@AllowanceHintAdapter.clients
            if (clients != null) {
                if (query == null || query.isEmpty()) {
                    values.addAll(clients)
                } else {
                    values.addAll(clients.filterCleaned(query) { c -> arrayOf(c.name, c.address) })
                }
            }
            results.count = values.size
            results.values = values
            return results
        }

        override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults?) {
            @Suppress("unchecked_cast")
            items = filterResults?.values as List<Any>?
        }
    }

}
