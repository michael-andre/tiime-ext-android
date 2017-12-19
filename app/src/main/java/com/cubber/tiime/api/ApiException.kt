package com.cubber.tiime.api

import com.cubber.tiime.model.ApiError
import java.io.IOException

/**
 * An exception carrying an API error details
 */
class ApiException(
        val statusCode: Int,
        val error: ApiError?
) : IOException(if (error != null) String.format("API error %s: %s (%s)", statusCode, error.code, error.message) else "API error " + statusCode)
