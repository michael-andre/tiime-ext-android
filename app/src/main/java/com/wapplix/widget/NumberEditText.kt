package com.wapplix.widget

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.*
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.Log
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

class NumberEditText : AffixEditText {

    private var decimalCompatListener: TextWatcher? = null
    private var internalFormat: NumberFormat = DecimalFormat.getNumberInstance()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var signed: Boolean
        get() = inputType and InputType.TYPE_NUMBER_FLAG_SIGNED == InputType.TYPE_NUMBER_FLAG_SIGNED
        set(signed) {
            val type = inputType
            inputType = if (signed) (type or InputType.TYPE_NUMBER_FLAG_SIGNED) else (type and InputType.TYPE_NUMBER_FLAG_SIGNED.inv())
        }

    var value: Number?
        get() {
            if (TextUtils.isEmpty(text)) return null
            return try {
                internalFormat.parse(text.toString())
            } catch (e: ParseException) {
                Log.e(NumberEditText::class.java.simpleName, "Invalid input", e)
                null
            }
        }
        set(value) = setText(if (value != null && value.toFloat().isFinite()) internalFormat.format(value) else null)

    fun applyFormat(source: NumberFormat) {
        val format = source.clone() as NumberFormat
        format.isGroupingUsed = false
        if (format is DecimalFormat) {
            // parseBigDecimal is not cloned in NativeDecimalFormat
            format.isParseBigDecimal = (source as DecimalFormat).isParseBigDecimal
            prefix = format.positivePrefix
            suffix = format.positiveSuffix
            format.positivePrefix = ""
            format.positiveSuffix = ""
            format.negativePrefix = "-"
            format.negativeSuffix = ""
            // Above values are not applied for parsing as for Android 24+, force it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                format.applyPattern(format.toPattern().replace("''", ""))
            }
        }
        internalFormat = format

        val type = inputType
        val decimal = format.maximumFractionDigits > 0
        inputType = if (decimal) type or InputType.TYPE_NUMBER_FLAG_DECIMAL else type and InputType.TYPE_NUMBER_FLAG_DECIMAL.inv()
    }

    override fun setInputType(type: Int) {
        super.setInputType(type or InputType.TYPE_CLASS_NUMBER)

        // Fix decimal separator on keyboard https://code.google.com/p/android/issues/detail?id=2626
        if (decimalCompatListener != null) removeTextChangedListener(decimalCompatListener)
        val format = internalFormat
        if (format is DecimalFormat && internalFormat.maximumFractionDigits > 0) {
            val decimalSeparator = format.decimalFormatSymbols.decimalSeparator
            if (decimalSeparator != '.') {
                keyListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    DigitsKeyListenerCompat(Locale.getDefault(), signed, decimalSeparator)
                } else {
                    DigitsKeyListenerCompat(signed, decimalSeparator)
                }
                if (decimalCompatListener == null) {
                    decimalCompatListener = object : TextWatcher {

                        internal val replace = Character.toString(decimalSeparator) + Character.toString(decimalSeparator)

                        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                        override fun afterTextChanged(editable: Editable) {
                            // Replace double decimal separator with minus (Samsung keyboard)
                            if (TextUtils.equals(editable, replace)) editable.replace(0, editable.length, "-")
                        }

                    }
                }
                addTextChangedListener(decimalCompatListener)
            }
        }
    }

    class DigitsKeyListenerCompat : DigitsKeyListener {

        private val acceptedChars: CharArray
        private val decimalSeparator: Array<String>
        private val signed: Boolean

        constructor(signed: Boolean, separator: Char) : super(signed, true) {
            acceptedChars = super.getAcceptedChars().plus(separator)
            decimalSeparator = arrayOf(Character.toString(separator))
            this.signed = signed
        }

        @RequiresApi(Build.VERSION_CODES.O)
        constructor(locale: Locale, signed: Boolean, separator: Char) : super(locale, signed, true) {
            acceptedChars = super.getAcceptedChars().plus(separator)
            decimalSeparator = arrayOf(Character.toString(separator))
            this.signed = signed
        }

        override fun getAcceptedChars(): CharArray {
            return acceptedChars
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
            var source = source
            var dest = dest
            source = TextUtils.replace(source, decimalSeparator, POINT)
            dest = SpannedString(TextUtils.replace(dest, decimalSeparator, POINT))
            // Allow double decimal separator (Samsung keyboard)
            if (signed && TextUtils.equals(POINT[0], source) && dest.length == 1 && POINT[0] == dest.toString()) {
                return decimalSeparator[0]
            }
            val out = super.filter(source, start, end, dest, dstart, dend)
            return if (out != null) {
                TextUtils.replace(out, POINT, decimalSeparator)
            } else {
                TextUtils.replace(source.subSequence(start, end), POINT, decimalSeparator)
            }
        }

        companion object {

            private val POINT = arrayOf(".")
        }

    }

}
