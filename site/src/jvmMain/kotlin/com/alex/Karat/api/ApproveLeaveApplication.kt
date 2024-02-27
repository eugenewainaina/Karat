package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.DepartmentLeave
import responses.DepartmentLeaveApiResponse
import java.util.*

// TODO check for a leave first
@Api
fun approveLeaveApplication(context: ApiContext) {
    if (context.req.method != HttpMethod.POST) return

    val adminID = context.req.params["admin_id"] ?: return
    val responseReason = context.req.params["response"]
    val status = context.req.params["status"] ?: return

    // deserialize the request body to DepartmentLeave object
    try {
        val leaveApplication = Json.decodeFromString<DepartmentLeave>(context.req.body!!.decodeToString())

        val updatedLeave =
            DatabaseFactory.updateLeave(
                leaveID = UUID.fromString(leaveApplication.leaveID),
                adminID = UUID.fromString(adminID),
                newResponseReason = responseReason,
                newStatus = status,
            )

        if (updatedLeave >= 0) {
            return context.res.setBodyText(
                Json.encodeToString<DepartmentLeaveApiResponse>(
                    DepartmentLeaveApiResponse.Success(
                        data =
                            listOf(
                                leaveApplication.apply {
                                    this.approvedBy = adminID
                                    this.status = status
                                    this.responseReason = responseReason
                                },
                            ),
                    ),
                ),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<DepartmentLeaveApiResponse>(
                DepartmentLeaveApiResponse.Error(
                    errorMessage = "Could not update leave. Database Error",
                ),
            ),
        )
    } catch (e: Exception) {
        context.res.setBodyText(
            Json.encodeToString<DepartmentLeaveApiResponse>(
                DepartmentLeaveApiResponse.Error(
                    errorMessage = e.message.toString().plus("ssss"),
                ),
            ),
        )
    }
}
