package com.alex.Karat.util

import kotlinx.browser.document
import kotlinx.browser.window

// called when logout button is clicked
fun logoutUser(path: String) {
    // a date in the past
    val expirationDate = "Thu, 01 Jan 1970 00:00:00 UTC"

    // by setting an expiry date of the past, the browser will consider the cookies expired and remove them
    // effectively deleting the cookies and thus logging the user out
    document.cookie = "user_id=; expires=$expirationDate; path=/$path"
    document.cookie = "email=; expires=$expirationDate; path=/$path"
    document.cookie = "organization_id=; expires=$expirationDate; path=/$path"
    document.cookie = "department_id=; expires=$expirationDate; path=/$path"
    document.cookie = "is_super_admin=; expires=$expirationDate; path=/$path"
    document.cookie = "is_admin=; expires=$expirationDate; path=/$path"

    window.location.replace("/login")
}
