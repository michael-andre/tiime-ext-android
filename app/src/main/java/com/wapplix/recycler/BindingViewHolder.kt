package com.wapplix.recycler

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

/**
 * Created by mike on 26/09/17.
 */
class BindingViewHolder<out VDB : ViewDataBinding>(val binding: VDB) : RecyclerView.ViewHolder(binding.root)
