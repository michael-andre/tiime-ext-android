package com.wapplix.arch

import android.arch.lifecycle.ViewModel
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction

/**
 * Created by mike on 11/12/17.
 */
interface ResultEmitter<T>

val <TFragment, T> TFragment.result: ResultModel<T> where TFragment : Fragment, TFragment : ResultEmitter<T>
    get() = getViewModel()

fun <TFragment, T> TFragment.show(fragmentManager: FragmentManager, tag: String, onResult: (T) -> Unit) where TFragment : DialogFragment, TFragment : ResultEmitter<T> {
    show(fragmentManager.beginTransaction(), tag, onResult)
}

fun <TFragment, T> TFragment.show(fragmentTransaction: FragmentTransaction, tag: String, onResult: (T) -> Unit) where TFragment : DialogFragment, TFragment : ResultEmitter<T> {
    show(fragmentTransaction.runOnCommit { result.onResult = onResult }, tag)
}

class ResultModel<T> : ViewModel() {
    lateinit var onResult: (T) -> Unit
}