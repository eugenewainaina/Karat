package com.alex.Karat.databaseOperations

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

// UUIDTable = a table where the primary key (by default "id") is of type UUID

// organizations table
object Organizations : UUIDTable("organization") {
    val organizationName = varchar("organization_name", 255)
}

// departments table
object Departments : UUIDTable("department") {
    val departmentName = varchar("department_name", 255)
    val organizationId = reference("organization_id", Organizations)
}

// users table
object Users : UUIDTable("user") {
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 255).uniqueIndex() // each email must be unique
    val password = varchar("password", 255)
    val phone = varchar("phone", 20)
    val organizationId = reference("organization_id", Organizations)
    val departmentId = reference("department_id", Departments).nullable() // super admins don't have departments, so can be null
    val isSuperAdmin = bool("is_super_admin")
    val isAdmin = bool("is_admin")
}

// leaves table
object Leaves : UUIDTable("leave") {
    val userId = reference("user_id", Users)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val duration = integer("duration")
    val applicationDate = date("application_date")
    val leaveType = varchar("leave_type", 50)
    val requestReason = text("request_reason").nullable() // user does not need to pass a reason
    val responseReason = text("response_reason").nullable() // admin does not need to pass a reason
    val status = varchar("status", 20)
    val approvedBy = reference("approved_by", Users).nullable()
}
