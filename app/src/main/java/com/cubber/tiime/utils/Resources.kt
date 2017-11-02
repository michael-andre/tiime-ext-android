@file:JvmName("Resources")

package com.cubber.tiime.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.content.res.AppCompatResources
import com.cubber.tiime.R
import com.cubber.tiime.model.Holiday
import com.cubber.tiime.model.Vehicle

fun vehicleTypeIcon(context: Context, @Vehicle.Type type: String?): Drawable? {
    return when (type) {
        Vehicle.TYPE_CAR -> AppCompatResources.getDrawable(context, R.drawable.ic_vehicle_type_car)
        Vehicle.TYPE_TWO_WHEELER_1 -> AppCompatResources.getDrawable(context, R.drawable.ic_vehicle_type_two_wheeler_1)
        Vehicle.TYPE_TWO_WHEELER_2 -> AppCompatResources.getDrawable(context, R.drawable.ic_vehicle_type_two_wheeler_2)
        else -> null
    }
}

fun vehicleTypeName(context: Context, @Vehicle.Type type: String?): String? {
    return when (type) {
        Vehicle.TYPE_CAR -> context.getString(R.string.vehicle_type_car)
        Vehicle.TYPE_TWO_WHEELER_1 -> context.getString(R.string.vehicle_type_two_wheeler_1)
        Vehicle.TYPE_TWO_WHEELER_2 -> context.getString(R.string.vehicle_type_two_wheeler_2)
        else -> null
    }
}

fun vehicleFiscalPowerName(context: Context, @Vehicle.FiscalPower power: String?): String? {
    return when (power) {
        Vehicle.FISCAL_POWER_3_OR_LESS -> return context.getString(R.string.vehicle_power_car_3_or_less)
        Vehicle.FISCAL_POWER_4 -> return context.getString(R.string.vehicle_power_car_4)
        Vehicle.FISCAL_POWER_5 -> return context.getString(R.string.vehicle_power_car_5)
        Vehicle.FISCAL_POWER_6 -> return context.getString(R.string.vehicle_power_car_6)
        Vehicle.FISCAL_POWER_7_OR_MORE -> return context.getString(R.string.vehicle_power_car_7_or_more)
        Vehicle.FISCAL_POWER_1_2 -> return context.getString(R.string.vehicle_power_two_wheeler_1_2)
        Vehicle.FISCAL_POWER_3_4_5 -> return context.getString(R.string.vehicle_power_two_wheeler_3_4_5)
        Vehicle.FISCAL_POWER_6_OR_MORE -> return context.getString(R.string.vehicle_power_two_wheeler_6_or_more)
        else -> null
    }
}

fun fileIcon(context: Context, uri: Uri?): Drawable? {
    if (uri == null || uri == Uri.EMPTY) return null
    val type = Uris.getMimeType(context, uri)
    return when {
        type == "application/pdf" -> AppCompatResources.getDrawable(context, R.drawable.ic_file_pdf)
        type == "application/msword" || type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> AppCompatResources.getDrawable(context, R.drawable.ic_file_document)
        type == "application/vnd.ms-excel" || type == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> AppCompatResources.getDrawable(context, R.drawable.ic_file_spreadsheet)
        type?.startsWith("image/") ?: false -> AppCompatResources.getDrawable(context, R.drawable.ic_file_image)
        else -> AppCompatResources.getDrawable(context, R.drawable.ic_file_generic)
    }
}

fun holidayTypeIndicator(context: Context, @Holiday.Type type: String?): Drawable? {
    return when (type) {
        Holiday.TYPE_COMPENSATORY_TIME -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_compensatory_time)
        Holiday.TYPE_FAMILY_MATTERS -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_family_matters)
        Holiday.TYPE_PAID_VACATION -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_paid_vacation)
        Holiday.TYPE_SICK_LEAVE -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_sick_leave)
        Holiday.TYPE_UNPAID_HOLIDAY -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_unpaid_holiday)
        Holiday.TYPE_WORK_ACCIDENT -> AppCompatResources.getDrawable(context, R.drawable.holiday_background_work_accident)
        else -> null
    }
}

fun holidayTypeName(context: Context, @Holiday.Type type: String?): String? {
    return when (type) {
        Holiday.TYPE_COMPENSATORY_TIME -> context.getString(R.string.holiday_type_compensatory_time)
        Holiday.TYPE_FAMILY_MATTERS -> context.getString(R.string.holiday_type_family_matters)
        Holiday.TYPE_PAID_VACATION -> context.getString(R.string.holiday_type_paid_vacation)
        Holiday.TYPE_SICK_LEAVE -> context.getString(R.string.holiday_type_sick_leave)
        Holiday.TYPE_UNPAID_HOLIDAY -> context.getString(R.string.holiday_type_unpaid_holidays)
        Holiday.TYPE_WORK_ACCIDENT -> context.getString(R.string.holiday_type_work_accident)
        else -> null
    }
}

fun selectableItemBackgroundBorderless(context: Context): Drawable? {
    val attrs = intArrayOf(R.attr.selectableItemBackgroundBorderless)
    val ta = context.obtainStyledAttributes(attrs)
    val background = ta.getDrawable(0)
    ta.recycle()
    return background
}