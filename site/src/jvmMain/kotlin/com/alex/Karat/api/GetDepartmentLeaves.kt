package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import responses.DepartmentLeaveApiResponse
import java.util.*

// gets the leaves in a particular department belonging to the admin
@Api
suspend fun getDepartmentLeaves(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    try {
        val departmentID = context.req.params["department_id"] ?: return
        val limit = context.req.params["limit"]?.toInt() ?: return
        val offset = context.req.params["offset"]?.toInt() ?: return

        // query the database for leaves in the department
        val departmentLeavesList =
            DatabaseFactory.getDepartmentLeaves(departmentID = UUID.fromString(departmentID), limit = limit, offset = offset)

        // if leaves have been found, return them
        if (departmentLeavesList.isNotEmpty()) {
            return context.res.setBodyText(
                Json.encodeToString<DepartmentLeaveApiResponse>(DepartmentLeaveApiResponse.Success(data = departmentLeavesList)),
            )
        }

        // if no leaves found, return an error
        return context.res.setBodyText(
            Json.encodeToString<DepartmentLeaveApiResponse>(
                DepartmentLeaveApiResponse.Error(
                    errorMessage = "No department leaves found",
                ),
            ),
        )
    } catch (e: Exception) {
        context.res.setBodyText(
            Json.encodeToString<DepartmentLeaveApiResponse>(
                DepartmentLeaveApiResponse.Error(errorMessage = e.message.toString()),
            ),
        )
    }
}
