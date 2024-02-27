package models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userID: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val isSuperAdmin: Boolean,
    val isAdmin: Boolean,
    val organizationID: String,
    val departmentID: String?
)