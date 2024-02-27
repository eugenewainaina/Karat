package com.alex.Karat.api

import com.alex.Karat.databaseOperations.DatabaseFactory
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText

@Api
fun addOrganization(context: ApiContext)  {
    if (context.req.method != HttpMethod.POST) return

    val orgName = context.req.params["organizationName"] ?: return

    val organizationID = DatabaseFactory.addOrganization(orgName)

    context.res.setBodyText(organizationID.toString())
}
