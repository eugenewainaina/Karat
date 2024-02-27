package models


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Leave(
    val leaveID: String = "",
    val userID: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val duration: Int,
    val leaveType: String,
    val applicationDate: LocalDate,
    val requestReason: String?,
    val responseReason: String?,
    val status: String,
    val approvedBy: String?
)


