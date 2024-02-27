package com.alex.Karat.util

import kotlinx.browser.document

// called whenever a page is loaded
// checks for the cookies to control access to certain pages
// if a user is trying to access an admin page but their cookie reads that they are an employee, access is denied
// also used to check if a user is logged in. If not, they are returned to the login page
fun getCookie(
    key: String,
    path: String,
): String? {
    val cookies = document.cookie.split("; ")

    cookies.forEach {
        val keyValuePairs = it.split("=")

        if (keyValuePairs[0].trim() == key && document.location!!.pathname.startsWith("/$path")) {
            return keyValuePairs[1].trim()
        }
    }

    return null
}

// sets the cookies for a particular path upon login
// if the logged-in user is an admin, the admin cookie is set to true. etc
fun setCookies(
    userID: String,
    email: String,
    organizationID: String,
    departmentID: String?,
    isSuperAdmin: Boolean,
    isAdmin: Boolean,
) {
    // sets the path of the cookie
    val path =
        if (isSuperAdmin) {
            "super_admin"
        } else if (isAdmin) {
            "admin"
        } else {
            "employee"
        }

    // cookies set
    document.cookie = "user_id=$userID; path=/$path"
    document.cookie = "email=$email; path=/$path"
    document.cookie = "organization_id=$organizationID; path=/$path"
    document.cookie = "department_id=$departmentID; path=/$path"
    document.cookie = "is_super_admin=$isSuperAdmin; path=/$path"
    document.cookie = "is_admin=$isAdmin; path=/$path"
}
