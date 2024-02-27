package models

import com.alex.kabosi.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Organization(
    /*@Serializable(with = UUIDSerializer::class)
    val organizationID: UUID = UUID.randomUUID(),*/

    val organizationName: String = ""
)
