package models

import com.alex.kabosi.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    /*@Serializable(with = UUIDSerializer::class)
    val userID: UUID = UUID.randomUUID(),*/

    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val accountType: String = "",
    val isSuperAdmin: Boolean = false,
    val isAdmin: Boolean = false,

    @Serializable(with = UUIDSerializer::class)
    val organizationID: UUID = UUID.randomUUID(),

    @Serializable(with = UUIDSerializer::class)
    val departmentID: UUID = UUID.randomUUID()
)