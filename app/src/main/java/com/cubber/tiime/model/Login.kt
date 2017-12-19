package com.cubber.tiime.model

data class Login(
        var id: Long = 0,
        var token: String? = null,
        var displayName: String? = null
)
