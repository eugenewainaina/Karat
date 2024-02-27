package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import responses.DepartmentApiResponse
import java.util.*

@Api
fun getDepartments(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    try {
        val organizationID = context.req.params["organization_id"] ?: return

        val departmentList = DatabaseFactory.getDepartments(organizationID = UUID.fromString(organizationID))

        if (departmentList.isNotEmpty()) {
            return context.res.setBodyText(
                Json.encodeToString<DepartmentApiResponse>(DepartmentApiResponse.Success(data = departmentList)),
            )
        } else {
            context.res.setBodyText(
                Json.encodeToString<DepartmentApiResponse>(
                    DepartmentApiResponse.Error(errorMessage = "No departments found"),
                ),
            )
        }
    } catch (e: Exception) {
        context.res.setBodyText(
            Json.encodeToString<DepartmentApiResponse>(
                DepartmentApiResponse.Error(errorMessage = e.message.toString()),
            ),
        )
    }
}
