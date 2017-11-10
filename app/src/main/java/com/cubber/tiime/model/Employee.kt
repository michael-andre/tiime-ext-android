package com.cubber.tiime.model

/**
 * Created by mike on 26/09/17.
 */

data class Employee(
        var id: Long = 0,
        var name: String? = null,
        var wagesValidationRequired: Boolean = false
)