package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Api
fun checkForPendingLeave(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    val userID = context.req.params["user_id"] ?: return

    val doesPendingLeaveExist = DatabaseFactory.checkPendingLeave(userID = UUID.fromString(userID))

    if (doesPendingLeaveExist) {
        context.res.setBodyText(Json.encodeToString(mapOf("exists" to true)))
    } else {
        context.res.setBodyText(Json.encodeToString(mapOf("exists" to false)))
    }
}
