package com.wapplix.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
import android.view.View

import com.cubber.tiime.R

open class AffixEditText @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.support.v7.appcompat.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var leftText: CharSequence? = null
    private var rightText: CharSequence? = null

    private var leftTextWidth = 0f
    private var rightTextWidth = 0f
    private val suffixPrefixPaint = Paint()

    private val isRtl: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && layoutDirection == View.LAYOUT_DIRECTION_RTL

    var prefix: CharSequence?
        get() = if (isRtl) rightText else leftText
        set(value) {
            if (isRtl) {
                rightText = value
            } else {
                leftText = value
            }
            requestLayout()
        }

    var suffix: CharSequence?
        get() = if (isRtl) leftText else rightText
        set(value) {
            if (isRtl) {
                leftText = suffix
            } else {
                rightText = suffix
            }
            requestLayout()
        }

    init {
        super.setSingleLine()
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AffixEditText, defStyleAttr, 0)
        prefix = a.getString(R.styleable.AffixEditText_prefix)
        suffix = a.getString(R.styleable.AffixEditText_suffix)
        a.recycle()
    }

    override fun setSingleLine(singleLine: Boolean) {
        // Force single line
        super.setSingleLine(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        leftTextWidth = if (leftText != null) paint.measureText(leftText!!.toString()) else 0f
        rightTextWidth = if (rightText != null) paint.measureText(rightText!!.toString()) else 0f
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(scrollX.toFloat(), 0f)
        suffixPrefixPaint.set(paint)
        if (leftText != null) {
            suffixPrefixPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(leftText!!, 0, leftText!!.length, super.getCompoundPaddingLeft().toFloat(), baseline.toFloat(), suffixPrefixPaint)
        }
        if (rightText != null) {
            suffixPrefixPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(rightText!!, 0, rightText!!.length, (width - super.getCompoundPaddingRight()).toFloat(), baseline.toFloat(), suffixPrefixPaint)
        }
    }

    override fun getCompoundPaddingLeft(): Int {
        return super.getCompoundPaddingLeft() + leftTextWidth.toInt()
    }

    override fun getCompoundPaddingRight(): Int {
        return super.getCompoundPaddingRight() + rightTextWidth.toInt()
    }

}
