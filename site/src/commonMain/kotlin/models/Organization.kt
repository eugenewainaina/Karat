package models

import kotlinx.serialization.Serializable

@Serializable
data class Organization(
    val organizationID: String,
    val organizationName: String
)