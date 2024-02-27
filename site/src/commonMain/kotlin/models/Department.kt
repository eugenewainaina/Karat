package com.alex.kabosi.models.tables

import com.alex.kabosi.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Department(
   /* @Serializable(with = UUIDSerializer::class)
    val departmentID: UUID = UUID.randomUUID(),*/

    val departmentName: String = ""
)
