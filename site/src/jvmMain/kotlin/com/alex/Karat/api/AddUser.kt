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

@Api
fun addUser(context: ApiContext) {
    if (context.req.method != HttpMethod.POST) return

    // deserialize the request body to User object
    try {
        val user = Json.decodeFromString<User>(context.req.body!!.decodeToString())

        val userID = DatabaseFactory.addUser(user)

        // if successful in adding a user
        if (userID != null) {
            return context.res.setBodyText(
                Json.encodeToString<UserApiResponse>(
                    UserApiResponse.Success(
                        data =
                            listOf(
                                user,
                            ),
                    ),
                ),
            )
        }

        // only executed if userID == null (if there was a database error)
        return context.res.setBodyText(
            Json.encodeToString<UserApiResponse>(
                UserApiResponse.Error(
                    errorMessage = "Could not add user. Database Error",
                ),
            ),
        )
    } catch (e: Exception) {
        println(e.message)
    }
}
