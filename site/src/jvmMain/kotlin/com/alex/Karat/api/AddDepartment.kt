package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Department
import responses.DepartmentApiResponse

// TODO check for a leave first
@Api
fun addDepartment(context: ApiContext) {
    if (context.req.method != HttpMethod.POST) return

    try {
        val department = Json.decodeFromString<Department>(context.req.body!!.decodeToString())

        // println("backend decoded department: ".plus(department))

        val departmentID = DatabaseFactory.addDepartment(department)

        if (departmentID != null) {
            return context.res.setBodyText(
                Json.encodeToString<DepartmentApiResponse>(
                    DepartmentApiResponse.Success(
                        data =
                            listOf(
                                Department(
                                    departmentID = departmentID.toString(),
                                    departmentName = department.departmentName,
                                    organizationID = department.organizationID,
                                ),
                            ),
                    ),
                ),
            )
        }

        return context.res.setBodyText(
            Json.encodeToString<DepartmentApiResponse>(
                DepartmentApiResponse.Error(
                    errorMessage = "Could not add department. Database Error",
                ),
            ),
        )
    } catch (e: Exception) {
        println(e.message)
    }
}
