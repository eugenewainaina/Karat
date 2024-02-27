package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Leave
import responses.LeaveApiResponse

// TODO check for a leave first
@Api
fun addLeave(context: ApiContext) {
    if (context.req.method != HttpMethod.POST) return

    // deserialize the request body to User object
    try {
        val leave = Json.decodeFromString<Leave>(context.req.body!!.decodeToString())

        val leaveID = DatabaseFactory.addLeave(leave)

        if (leaveID != null) {
            return context.res.setBodyText(
                Json.encodeToString<LeaveApiResponse>(
                    LeaveApiResponse.Success(
                        data =
                            listOf(
                                leave,
                            ),
                    ),
                ),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<LeaveApiResponse>(
                LeaveApiResponse.Error(
                    errorMessage = "Could not add leave. Database Error",
                ),
            ),
        )
    } catch (e: Exception) {
        println(e.message)
    }
}
