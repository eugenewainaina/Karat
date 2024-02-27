package models

import com.alex.kabosi.util.UUIDSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.*
import kotlin.time.Duration

@Serializable
data class Leave(
    /*@Serializable(with = UUIDSerializer::class)
    val leaveID: UUID = UUID.randomUUID(),*/

    @Serializable(with = UUIDSerializer::class)
    val userID: UUID = UUID.randomUUID(),

    val startDate: LocalDate = LocalDate.parse(""),
    val endDate: LocalDate = LocalDate.parse(""),
    val duration: Int = 0,
    val applicationDate: LocalDate = LocalDate.parse(""),
    val requestReason: String? = "",
    val responseReason: String? = "",
    val status: String = "",

    @Serializable(with = UUIDSerializer::class)
    val approvedBy: UUID = UUID.randomUUID()
)


