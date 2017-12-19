package com.cubber.tiime.model

data class ApiError(
    var code: String? = null,
    var message: String? = null
) {

    companion object {

        val CODE_INVALID_AUTHENTICATION = "invalidAuthentication"
        val CODE_INVALID_PASSWORD = "invalidPassword"
        val CODE_INVALID_CURRENT_PASSWORD = "invalidCurrentPassword"
    }

}
