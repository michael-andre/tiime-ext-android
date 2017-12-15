package com.wapplix.arch

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

/**
 * Created by mike on 12/12/17.
 */
fun TextView.addTextChangedListener(
        afterTextChanged: ((s: Editable?) -> Unit)? = null,
        beforeTextChanged: ((s: CharSequence?, start: Int, count: Int, after: Int) -> Unit)? = null,
        onTextChanged: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null
) {
    addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            afterTextChanged?.invoke(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged?.invoke(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged?.invoke(s, start, before, count)
        }

    })
}

fun TextView.setOnEditorActionListener(
        onDone: ((KeyEvent?) -> Boolean)? = null,
        onNext: ((KeyEvent?) -> Boolean)? = null,
        onSearch: ((KeyEvent?) -> Boolean)? = null,
        onEnter: ((KeyEvent?) -> Boolean)? = null
) {
    setOnEditorActionListener { _, actionId, keyEvent ->
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> onDone?.invoke(keyEvent) ?: false
            EditorInfo.IME_ACTION_NEXT -> onNext?.invoke(keyEvent) ?: false
            EditorInfo.IME_ACTION_SEARCH -> onSearch?.invoke(keyEvent) ?: false
            EditorInfo.IME_NULL -> onEnter?.invoke(keyEvent) ?: false
            else -> false
        }
    }
}