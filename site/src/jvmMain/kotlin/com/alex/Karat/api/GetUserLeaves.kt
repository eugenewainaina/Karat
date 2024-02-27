package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import responses.LeaveApiResponse
import responses.UserApiResponse
import java.util.*

// gets the leaves of a particular employee/user
@Api
suspend fun getUserLeaves(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    try {
        val userID = context.req.params["user_id"] ?: return
        val limit = context.req.params["limit"]?.toInt() ?: return
        val offset = context.req.params["offset"]?.toInt() ?: return

        val userLeaveList =
            DatabaseFactory.getUserLeaves(userID = UUID.fromString(userID), limit = limit, offset = offset)

        if (userLeaveList.isNotEmpty()) {
            return context.res.setBodyText(
                Json.encodeToString<LeaveApiResponse>(LeaveApiResponse.Success(data = userLeaveList)),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<LeaveApiResponse>(
                LeaveApiResponse.Error(
                    errorMessage = "No leaves found",
                ),
            ),
        )
    } catch (e: Exception) {
        context.res.setBodyText(
            Json.encodeToString<UserApiResponse>(
                UserApiResponse.Error(errorMessage = e.message.toString().plus(" excepepepe")),
            ),
        )
    }
}
