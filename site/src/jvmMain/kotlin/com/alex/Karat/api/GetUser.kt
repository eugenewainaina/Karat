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
fun getUser(context: ApiContext) {
    if (context.req.method != HttpMethod.GET) return

    try {
        val email = context.req.params["email"] ?: return
        val password = context.req.params["password"] ?: return

        val userList: List<User> = DatabaseFactory.getUser(email)

        // if a user was found, the list will contain one user
        if (userList.isNotEmpty() && userList[0].password == password) {
            return context.res.setBodyText(
                Json.encodeToString<UserApiResponse>(UserApiResponse.Success(data = userList)),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<UserApiResponse>(
                UserApiResponse.Error(
                    errorMessage = if (userList.isEmpty()) "No users found of email $email" else "Incorrect password",
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
