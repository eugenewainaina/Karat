package models

import kotlinx.serialization.Serializable

@Serializable
data class Department(
    val departmentID: String,
    val departmentName: String,
    val organizationID: String,
)
