package com.application.expertnewdesign.authorization.data.model

import kotlinx.serialization.Serializable

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Serializable
data class LoggedInUser(
    val userToken: String,
    val displayName: String? = null
)
