package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Api
fun checkIfEmailExists(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    val email = context.req.params["email"] ?: return

    val doesEmailExist = DatabaseFactory.doesEmailExist(email = email)

    if (doesEmailExist) {
        context.res.setBodyText(Json.encodeToString(mapOf("exists" to true)))
    } else {
        context.res.setBodyText(Json.encodeToString(mapOf("exists" to false)))
    }
}
