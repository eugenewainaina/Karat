package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.User
import responses.UserApiResponse
import java.util.*

@Api
fun getUsers(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    try {
        val departmentID = context.req.params["department_id"] ?: return
        val limit = context.req.params["limit"]?.toInt() ?: return
        val offset = context.req.params["offset"]?.toInt() ?: return
        val isAdmin = context.req.params["is_admin"]?.toBoolean() ?: return
        val isSuperAdmin = context.req.params["is_super_admin"]?.toBoolean() ?: return

        val employeesList: List<User> =
            DatabaseFactory.getUsers(
                departmentID = UUID.fromString(departmentID),
                isAdmin = isAdmin,
                isSuperAdmin = isSuperAdmin,
                limit = limit,
                offset = offset,
            )

        if (employeesList.isNotEmpty()) {
            return context.res.setBodyText(
                Json.encodeToString<UserApiResponse>(UserApiResponse.Success(data = employeesList)),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<UserApiResponse>(
                UserApiResponse.Error(
                    errorMessage = "No employees found",
                ),
            ),
        )
    } catch (e: Exception) {
        context.res.setBodyText(
            Json.encodeToString<UserApiResponse>(
                UserApiResponse.Error(errorMessage = e.message.toString()),
            ),
        )
    }
}
