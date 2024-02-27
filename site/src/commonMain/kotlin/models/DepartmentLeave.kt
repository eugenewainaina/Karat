package models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DepartmentLeave (
    val employeeID: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val leaveID: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val applicationDate: LocalDate,
    val duration: Int,
    val leaveType: String,
    val requestReason: String?,
    var responseReason: String?,
    var status: String,
    var approvedBy: String?
)